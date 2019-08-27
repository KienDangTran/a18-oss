package com.a18.lottery.util;

import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Rule;
import com.a18.lottery.model.dto.BetItemGroupDTO;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.Assert;

@UtilityClass
public class TicketUtil {

  /**
   * groups given bet items by bet unit
   */
  public Set<BetItemGroupDTO> regroupBetItemsByBetUnit(Map<String, Integer> betItems) {
    if (betItems == null || betItems.isEmpty()) {
      return Set.of();
    }
    return Set.copyOf(
        betItems
            .entrySet()
            .stream()
            .collect(Collectors.groupingBy(
                Entry::getValue,
                Collectors.mapping(
                    item -> new BetItemGroupDTO(item.getValue(), item.getKey(), Set.of()),
                    Collectors.reducing(null, TicketUtil::reduceBetItemGroup)
                )
            ))
            .values()
    );
  }

  public static BetItemGroupDTO reduceBetItemGroup(BetItemGroupDTO gr1, BetItemGroupDTO gr2) {
    if (gr1 == null) return gr2;
    if (gr2 == null) return gr1;
    if (!gr1.getBetUnit().equals(gr2.getBetUnit())) {
      throw new IllegalArgumentException("cannot merge 2 bet item groups with different bet unit");
    }
    return new BetItemGroupDTO(
        gr1.getBetUnit(),
        StringUtils.joinWith(
            BetItemGroup.ITEM_CONTENTS_DELIMITER,
            gr1.getBetItems(),
            gr2.getBetItems()
        ),
        Stream.concat(gr1.getWonItems().stream(), gr2.getWonItems().stream())
              .collect(Collectors.toUnmodifiableSet())
    );
  }

  public static Map<String, Integer> collectBetItems(Map<Integer, String> betItemGroups) {
    Assert.notEmpty(betItemGroups, "cannot collectBetItems 'coz betItemGroup is empty");

    return betItemGroups
        .entrySet()
        .stream()
        .filter(gr -> StringUtils.isNotBlank(gr.getValue())
            && gr.getKey() != null
            && gr.getKey() > 0)
        .flatMap(group ->
            breakdownBetItemGroup(group.getValue(), group.getKey()).entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, Integer::sum));
  }

  private Map<String, Integer> breakdownBetItemGroup(String betItems, Integer betUnit) {
    if (StringUtils.isBlank(betItems) || NumberUtils.compare(betUnit, 0) <= 0) {
      return Map.of();
    }

    String[] betContents =
        StringUtils.split(betItems, BetItemGroup.ITEM_CONTENTS_DELIMITER);

    return Arrays.stream(betContents)
                 .map(betContent -> new AbstractMap.SimpleImmutableEntry<>(
                     trimBetContent(betContent),
                     betUnit
                 ))
                 .collect(Collectors.toMap(
                     AbstractMap.SimpleImmutableEntry::getKey,
                     AbstractMap.SimpleImmutableEntry::getValue,
                     Integer::sum
                 ));
  }

  /**
   * trim whitespaces in bet content
   */
  private static String trimBetContent(String betContent) {
    Assert.isTrue(StringUtils.isNotBlank(betContent), "content of a BetItemGroup cannot be blank");
    return Arrays.stream(StringUtils.split(betContent, BetItemGroup.BET_NUMBERS_DELIMITER))
                 .map(StringUtils::trimToEmpty)
                 .collect(Collectors.joining(BetItemGroup.BET_NUMBERS_DELIMITER));
  }

  public static int countWinTime(
      String betContent,
      Set<Rule> rules,
      Set<DrawResult> drawResults
  ) {
    Assert.isTrue(
        StringUtils.isNotBlank(betContent),
        "cannot countWinTime 'coz betContent is blank"
    );
    Assert.notEmpty(rules, "cannot countWinTime 'coz rules is empty");
    Assert.notEmpty(drawResults, "cannot countWinTime 'coz rules is drawResults");

    Map<String, Long> betNoMatchedTime = new HashMap<>();
    Set<String> betNumbers =
        Set.of(StringUtils.split(betContent, BetItemGroup.BET_NUMBERS_DELIMITER));
    betNumbers.forEach(betNo -> rules.forEach(rule -> {
      Integer[] checkPositions = getCheckPositions(rule);
      drawResults
          .stream()
          .filter(result -> rule.getPrizeSchemasId() != null
              && rule.getPrizeSchemasId().equals(result.getPrize().getPrizeSchema().getId()))
          .forEach(result -> {
            String[] comparedValues = getComparedValuesInAllWinNumbers(
                StringUtils.split(result.getWinNo(), DrawResult.WIN_NO_DELIMITER),
                checkPositions
            );

            long matchedTime =
                countBetNoMatchedTime(StringUtils.trimToEmpty(betNo), comparedValues);
            betNoMatchedTime.merge(betNo, matchedTime, Long::sum);
          });
    }));

    return Math.toIntExact(betNoMatchedTime.values().stream().min(Long::compareTo).orElse(0L));
  }

  /**
   * collect check positions that use for comparing bet numbers and {@link DrawResult#getWinNo()}
   */
  private static Integer[] getCheckPositions(Rule rule) {
    return Arrays.stream(StringUtils.split(rule.getCheckPositions(), Rule.CHECK_POSITION_DELIMITER))
                 .filter(StringUtils::isNotBlank)
                 .filter(NumberUtils::isDigits)
                 .map(NumberUtils::toInt)
                 .filter(checkPos -> NumberUtils.compare(checkPos, 1) >= 0)
                 .distinct()
                 .sorted(NumberUtils::compare)
                 .toArray(Integer[]::new);
  }

  /**
   * <pre>
   *   from each win number of given winNumbers:
   *   - if it's length less than {@param checkPositions}'s length -> skip
   *   - collect digits at corresponding check positions
   * </pre>
   *
   * @param winNumbers: all win number from a specific {@link DrawResult#getWinNo()}
   * @param checkPositions: array of check positions of a {@link Rule#getCheckPositions()}
   */
  private static String[] getComparedValuesInAllWinNumbers(
      String[] winNumbers,
      Integer[] checkPositions
  ) {
    return Arrays
        .stream(winNumbers)
        .filter(winNo -> StringUtils.trimToEmpty(winNo).length() >= checkPositions.length)
        .map(winNo -> {
          StringBuilder partOfResultToMatch = new StringBuilder();
          Arrays.stream(checkPositions).forEach(
              pos -> partOfResultToMatch.append(StringUtils.trimToEmpty(winNo).charAt(pos - 1))
          );
          return partOfResultToMatch.toString();
        })
        .toArray(String[]::new);
  }

  private static long countBetNoMatchedTime(String betNo, String[] comparedValues) {
    return Arrays.stream(comparedValues).filter(betNo::equalsIgnoreCase).count();
  }
}
