package com.a18.lottery.util;

import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Rule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.springframework.util.Assert;

@Slf4j
@UtilityClass
public class QuickLotteryResultGenerator {

  private final Character[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

  private final SecureRandom secureRandom = new SecureRandom();

  public static String genAllWinNumbersRandomly(int winNoSize, int winNoLength) {
    Assert.isTrue(
        winNoSize > 0,
        "cannot genAllWinNumbersRandomly 'coz winNoSize is invalid"
    );
    Assert.isTrue(
        winNoLength > 0,
        "cannot genAllWinNumbersRandomly 'coz winNoLength is invalid"
    );

    return IntStream.range(0, winNoSize)
                    .mapToObj(i -> QuickLotteryResultGenerator.genRandomWinNumber(winNoLength))
                    .collect(Collectors.joining(DrawResult.WIN_NO_DELIMITER));
  }

  private static String genRandomWinNumber(int length) {
    return IntStream.range(0, length)
                    .mapToObj(i -> digits[secureRandom.nextInt(digits.length)])
                    .map(String::valueOf)
                    .collect(Collectors.joining());
  }

  public static String genWinNumbersBaseOnWeightOfBetContents(
      int winNoSize,
      int winNoLength,
      Map<String, BigDecimal> betContentsWithPotentialWinningAmt,
      Set<Rule> rules
  ) {
    if (betContentsWithPotentialWinningAmt.isEmpty() || rules.isEmpty()) {
      return genAllWinNumbersRandomly(winNoSize, winNoLength);
    }

    Map<Integer, Map<Character, BigDecimal>> checkPositionsInAllRulesWithProbabilityOfEachDigit =
        rules.stream()
             .map(Rule::getCheckPositions)
             .map(r -> StringUtils.split(r, Rule.CHECK_POSITION_DELIMITER))
             .flatMap(Stream::of)
             .filter(StringUtils::isNotBlank)
             .filter(NumberUtils::isDigits)
             .map(NumberUtils::toInt)
             .filter(checkPos -> NumberUtils.compare(checkPos, 1) >= 0)
             .distinct()
             .map(checkPos -> new AbstractMap.SimpleEntry<>(
                 checkPos - 1,
                 calcProbabilityForEachDigit(
                     winNoLength,
                     checkPos - 1,
                     betContentsWithPotentialWinningAmt
                 )
             ))
             .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (m1, m2) -> m1));

    log.debug("{}", checkPositionsInAllRulesWithProbabilityOfEachDigit);

    return IntStream.range(0, winNoSize)
                    .mapToObj(i -> QuickLotteryResultGenerator.genWinNumber(
                        winNoLength,
                        checkPositionsInAllRulesWithProbabilityOfEachDigit
                    ))
                    .collect(Collectors.joining(DrawResult.WIN_NO_DELIMITER));
  }

  private static Map<Character, BigDecimal> calcProbabilityForEachDigit(
      int length,
      int drawPosition,
      Map<String, BigDecimal> betContentsWithPotentialWinningAmt
  ) {
    BigDecimal totalPotentialWinningAmt =
        betContentsWithPotentialWinningAmt.values()
                                          .stream()
                                          .reduce(BigDecimal.ZERO, BigDecimal::add);
    return betContentsWithPotentialWinningAmt
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().length() >= length - drawPosition)
        .map(entry -> new AbstractMap.SimpleEntry<>(
            StringUtils.leftPad(entry.getKey(), length).charAt(drawPosition),
            entry.getValue()
        ))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> BigDecimal.ONE.subtract(
                entry.getValue().divide(totalPotentialWinningAmt, 4, RoundingMode.UP)
            ),
            (amt1, amt2) -> BigDecimal.ONE.subtract(
                (amt1.add(amt2)).divide(totalPotentialWinningAmt, 4, RoundingMode.UP)
            )
        ));
  }

  private String genWinNumber(
      int length,
      Map<Integer, Map<Character, BigDecimal>> checkPositionsInAllRulesWithProbabilityOfEachDigit
  ) {
    return IntStream
        .range(0, length)
        .mapToObj(drawPos -> randomByProbability(
            drawPos,
            checkPositionsInAllRulesWithProbabilityOfEachDigit
        ))
        .map(String::valueOf)
        .collect(Collectors.joining());
  }

  private static char randomByProbability(
      int drawPosition,
      Map<Integer, Map<Character, BigDecimal>> checkPositionsInAllRulesWithProbabilityOfEachDigit
  ) {
    if (!checkPositionsInAllRulesWithProbabilityOfEachDigit.containsKey(drawPosition)) {
      return digits[secureRandom.nextInt(digits.length)];
    }

    Map<Character, BigDecimal> characterBigDecimalMap =
        checkPositionsInAllRulesWithProbabilityOfEachDigit.get(drawPosition);

    List<Pair<Character, Double>> samples =
        Arrays.stream(digits)
              .map(digit -> new Pair<>(
                  digit,
                  characterBigDecimalMap.containsKey(digit)
                  ? characterBigDecimalMap.get(digit).doubleValue()
                  : 1
              ))
              .collect(Collectors.toList());

    return new EnumeratedDistribution<>(samples).sample();
  }
}
