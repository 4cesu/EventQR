package com.thedavelopers.eventqr.features.rewards.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionScanRequest;
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionScanResponse;
import com.thedavelopers.eventqr.features.rewards.service.RewardRedemptionScanService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardRedemptionScanController {

    private final RewardRedemptionScanService rewardRedemptionScanService;
    private final JwtService jwtService;

    public RewardRedemptionScanController(RewardRedemptionScanService rewardRedemptionScanService,
                                          JwtService jwtService) {
        this.rewardRedemptionScanService = rewardRedemptionScanService;
        this.jwtService = jwtService;
    }

    @PostMapping("/redemption-scan")
    public ResponseEntity<ApiResponse<RewardRedemptionScanResponse>> scan(HttpServletRequest request,
                                                                          @Valid @RequestBody RewardRedemptionScanRequest body) {
        requireStaffRole(request);
        return ResponseEntity.ok(ApiResponse.success("Reward redemption scan recorded",
                rewardRedemptionScanService.scan(body)));
    }

    private void requireStaffRole(HttpServletRequest request) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role != AccountRole.STAFF && role != AccountRole.ORGANIZER && role != AccountRole.ADMIN) {
            throw new ForbiddenException("Staff access required");
        }
    }
}
