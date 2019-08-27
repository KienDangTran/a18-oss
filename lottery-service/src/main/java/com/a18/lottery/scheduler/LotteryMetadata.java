package com.a18.lottery.scheduler;

import com.a18.common.constant.GameCategory;
import com.a18.common.dto.LotteryDTO;
import com.a18.common.dto.PrizeDTO;
import com.a18.common.firebase.FirestoreUtils;
import com.a18.lottery.model.repository.LotteryRepository;
import com.a18.lottery.model.repository.LotterySchemaRepository;
import com.a18.lottery.model.repository.PrizeRepository;
import com.a18.lottery.model.repository.PrizeSchemaRepository;
import com.a18.lottery.model.repository.SchedulerRepository;
import com.a18.lottery.util.LotterySchemaUtil;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class LotteryMetadata {

  @Autowired @Lazy SchedulerRepository schedulerRepository;

  @Autowired @Lazy LotterySchemaRepository lotterySchemaRepository;

  @Autowired @Lazy LotteryRepository lotteryRepository;

  @Autowired @Lazy PrizeSchemaRepository prizeSchemaRepository;

  @Autowired @Lazy PrizeRepository prizeRepository;

  @Autowired @Lazy FirestoreUtils firestoreUtils;

  @PostConstruct
  public void initLotteryMetadataInFireStore() {
    this.schedulerRepository
        .findAll()
        .forEach(scheduler -> this.firestoreUtils.addData(
            scheduler,
            GameCategory.LOTTERY.name().toLowerCase(),
            "metadata",
            "scheduler",
            scheduler.getId().toString()
        ));

    this.lotteryRepository
        .findAll()
        .forEach(lottery -> {
          LotteryDTO dto =
              LotteryDTO.builder()
                        .id(lottery.getId())
                        .code(lottery.getCode())
                        .lotterySchemaId(lottery.getLotterySchemaId())
                        .schedulerId(lottery.getSchedulerId())
                        .betUnitPrice(LotterySchemaUtil.getLotteryBetUnitPrice(lottery))
                        .winUnitPrice(LotterySchemaUtil.getLotteryWinUnitPrice(lottery))
                        .maxBetItem(LotterySchemaUtil.getLotteryMaxBetItem(lottery))
                        .betItemMaxAmt(LotterySchemaUtil.getLotteryBetItemMaxAmt(lottery))
                        .maxPayout(LotterySchemaUtil.getLotteryMaxPayout(lottery))
                        .betItemSize(LotterySchemaUtil.getLotteryBetItemSize(lottery))
                        .betNoLength(LotterySchemaUtil.getLotteryBetNoLength(lottery))
                        .build();
          this.firestoreUtils.addData(
              dto,
              GameCategory.LOTTERY.name().toLowerCase(),
              "metadata",
              "scheduler",
              lottery.getSchedulerId().toString(),
              "lottery",
              lottery.getId().toString()
          );
        });

    this.prizeRepository
        .findAll()
        .forEach(prize -> {
          PrizeDTO dto =
              PrizeDTO.builder()
                      .id(prize.getId())
                      .code(prize.getCode())
                      .prizeSchemaId(prize.getPrizeSchemaId())
                      .schedulerId(prize.getSchedulerId())
                      .prizePosition(prize.getPrizePosition())
                      .winNoSize(prize.getPrizeSchema().getWinNoSize())
                      .winNoLength(prize.getPrizeSchema().getWinNoLength())
                      .build();
          this.firestoreUtils.addData(
              dto,
              GameCategory.LOTTERY.name().toLowerCase(),
              "metadata",
              "scheduler",
              prize.getSchedulerId().toString(),
              "prize",
              prize.getId().toString()
          );
        });

  }
}
