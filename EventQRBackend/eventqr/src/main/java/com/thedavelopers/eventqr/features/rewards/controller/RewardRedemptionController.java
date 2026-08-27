package com.thedavelopers.eventqr.features.rewards.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionGrantRequest;
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionResultResponse;
import com.thedavelopers.eventqr.features.rewards.service.RewardRedemptionService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardRedemptionController {

    private final RewardRedemptionService rewardRedemptionService;
    private final JwtService jwtService;

    public RewardRedemptionController(RewardRedemptionService rewardRedemptionService,
                                      JwtService jwtService) {
        this.rewardRedemptionService = rewardRedemptionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/redeem-staff")
    public ResponseEntity<ApiResponse<RewardRedemptionResultResponse>> redeem(HttpServletRequest request,
                                                                              @Valid @RequestBody RewardRedemptionGrantRequest body) {
        requireStaffRole(request);
        RewardRedemptionResultResponse result = rewardRedemptionService.redeem(body);
        return ResponseEntity.ok(ApiResponse.success(
                result.approved() ? "Reward redeemed" : "Reward redemption rejected", result));
    }

    private void requireStaffRole(HttpServletRequest request) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role != AccountRole.STAFF && role != AccountRole.ORGANIZER && role != AccountRole.ADMIN) {
            throw new ForbiddenException("Staff access required");
        }
    }
}
