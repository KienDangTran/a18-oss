package com.a18.lottery.controller;

import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.dto.BetItemGroupDTO;
import com.a18.lottery.model.repository.BetItemGroupRepository;
import com.a18.lottery.model.repository.WonItemRepository;
import com.a18.lottery.specification.BetItemGroupSpecification;
import com.a18.lottery.util.TicketUtil;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.a18.lottery.model.BetItemGroup.ITEM_CONTENTS_DELIMITER;

@RestController
public class BetItemGroupController {

  private static final String PATH_BET_ITEM_GROUPS = "/betItemGroups";

  @Autowired @Lazy private BetItemGroupRepository betItemGroupRepository;

  @Autowired @Lazy private WonItemRepository wonItemRepository;

  @GetMapping(PATH_BET_ITEM_GROUPS)
  public ResponseEntity findBySpec(
      @RequestParam Long ticketId,
      @RequestParam(required = false) String[] betItems,
      @PageableDefault Pageable pageable
  ) {
    Page<BetItemGroup> betItemGroups = this.betItemGroupRepository.findAll(
        BetItemGroupSpecification.filterBetItemGroup(ticketId, betItems),
        pageable
    );

    Set<BetItemGroupDTO> grDTOs =
        TicketUtil
            .regroupBetItemsByBetUnit(TicketUtil.collectBetItems(
                betItemGroups
                    .getContent()
                    .stream()
                    .collect(Collectors.toUnmodifiableMap(
                        BetItemGroup::getBetUnit,
                        BetItemGroup::getBetItems,
                        (gr1, gr2) -> StringUtils.joinWith(ITEM_CONTENTS_DELIMITER, gr1, gr2)
                    ))
            ))
            .stream()
            .map(grDTO -> new BetItemGroupDTO(
                grDTO.getBetUnit(),
                grDTO.getBetItems(),
                this.wonItemRepository.findAllByTicketIdAndAndBetContentIn(
                    ticketId,
                    Set.of(StringUtils.split(
                        grDTO.getBetItems(),
                        ITEM_CONTENTS_DELIMITER
                    ))
                )
            ))
            .collect(Collectors.toUnmodifiableSet());

    return ResponseEntity.ok(this.createPagedResource(new PageImpl<>(
        new ArrayList<>(grDTOs),
        pageable,
        grDTOs.size()
    )));
  }

  private PagedResources<BetItemGroupDTO> createPagedResource(Page<BetItemGroupDTO> page) {
    return new PagedResources<>(
        page.getContent(),
        new PagedResources.PageMetadata(
            page.getSize(),
            page.getNumber(),
            page.getTotalElements(),
            page.getTotalPages()
        )
    );
  }
}
