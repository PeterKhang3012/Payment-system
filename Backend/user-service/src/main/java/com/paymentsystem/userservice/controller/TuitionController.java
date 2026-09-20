package com.paymentsystem.userservice.controller;

import com.paymentsystem.userservice.dto.UserDtos.TuitionResponse;
import com.paymentsystem.userservice.dto.UserDtos.UpdateTuitionStatusRequest;
import com.paymentsystem.userservice.service.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tra cứu và cập nhật học phí.
 * GET: yêu cầu JWT. PATCH /{id}/status: xác thực bằng X-Internal-Service-Token.
 */
@RestController
@RequestMapping("/api/tuitions")
@RequiredArgsConstructor
@Tag(name = "Tuitions", description = "Tra cuu hoc phi va cap nhat trang thai thanh toan")
public class TuitionController {

    private final TuitionService tuitionService;

    @GetMapping("/me")
    @Operation(summary = "Lay hoc phi cua sinh vien dang dang nhap",
               security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Danh sach hoc phi"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le")
    })
    public ResponseEntity<List<TuitionResponse>> getMyTuitions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tuitionService.getTuitionsByCurrentUser(userDetails));
    }

    @GetMapping
    @Operation(summary = "Lay hoc phi theo studentId",
               description = "Dung boi payment-service hoac admin.",
               security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Danh sach hoc phi"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le")
    })
    public ResponseEntity<List<TuitionResponse>> getTuitionsByStudentId(@RequestParam String studentId) {
        return ResponseEntity.ok(tuitionService.getTuitionsByStudentId(studentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiet khoan hoc phi",
               security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chi tiet hoc phi"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le"),
        @ApiResponse(responseCode = "404", description = "Khong tim thay")
    })
    public ResponseEntity<TuitionResponse> getTuitionById(@PathVariable Long id) {
        return ResponseEntity.ok(tuitionService.getTuitionById(id));
    }

    /**
     * Cập nhật trạng thái học phí sang PAID sau thanh toán.
     * Dual auth: JWT (test trực tiếp) hoặc X-Internal-Service-Token (payment-service).
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Cap nhat trang thai hoc phi sang PAID",
        description = "API noi bo danh cho payment-service. Dung atomic UPDATE de ngan double-payment.\n"
                    + "Header: X-Internal-Service-Token: ps-internal-service-token-2025\n"
                    + "Body: { \"status\": \"PAID\", \"paymentId\": \"TXN-...\" }")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gach no thanh cong"),
        @ApiResponse(responseCode = "400", description = "status khong phai PAID hoac paymentId trong"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le"),
        @ApiResponse(responseCode = "404", description = "Khong tim thay hoc phi"),
        @ApiResponse(responseCode = "409", description = "Hoc phi da PAID — double-payment bi chan")
    })
    public ResponseEntity<TuitionResponse> updateTuitionStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTuitionStatusRequest request,
            @RequestHeader(value = "X-Internal-Service-Token", required = false) String serviceToken,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            tuitionService.validateServiceToken(serviceToken);
        }

        return ResponseEntity.ok(tuitionService.updateTuitionStatus(id, request));
    }
}
