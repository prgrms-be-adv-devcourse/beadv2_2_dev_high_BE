package com.dev_high.admin.presentation;

import com.dev_high.admin.presentation.applicaiton.AdminService;
import com.dev_high.auction.application.AuctionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auctions")
@Tag(name = "Auction", description = "경매 관리 API")
public class AdminController {

  private final AuctionService auctionService;
    private final AdminService adminService;


}
