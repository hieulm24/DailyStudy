package com.englishlearning.service.financial;

import com.englishlearning.common.BadRequestException;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.financial.GoalDtos.*;
import com.englishlearning.entity.financial.FinancialAccount;
import com.englishlearning.entity.financial.FinancialGoal;
import com.englishlearning.entity.financial.FinancialGoalContribution;
import com.englishlearning.repository.financial.FinancialAccountRepository;
import com.englishlearning.repository.financial.FinancialGoalContributionRepository;
import com.englishlearning.repository.financial.FinancialGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialGoalService {

    private final FinancialGoalRepository goalRepository;
    private final FinancialGoalContributionRepository contributionRepository;
    private final FinancialAccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<GoalResponse> getAllGoals(String status) {
        List<FinancialGoal> goals;
        if (status != null && !status.isBlank()) {
            goals = goalRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        } else {
            goals = goalRepository.findAllByOrderByCreatedAtDesc();
        }

        return goals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoalById(Long id) {
        FinancialGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục tiêu tài chính với ID: " + id));
        return mapToResponse(goal);
    }

    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        validateGoalRequest(request);

        if (goalRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Mục tiêu có tên '" + request.getName().trim() + "' đã tồn tại");
        }

        FinancialGoal goal = FinancialGoal.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .targetDate(request.getTargetDate())
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().toUpperCase() : "ACTIVE")
                .icon(request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "Target")
                .color(request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : "#2563eb")
                .build();

        FinancialGoal saved = goalRepository.save(goal);
        return mapToResponse(saved);
    }

    @Transactional
    public GoalResponse updateGoal(Long id, GoalRequest request) {
        validateGoalRequest(request);

        FinancialGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục tiêu tài chính với ID: " + id));

        if (goalRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new BadRequestException("Mục tiêu có tên '" + request.getName().trim() + "' đã tồn tại");
        }

        goal.setName(request.getName().trim());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        if (request.getStartDate() != null) goal.setStartDate(request.getStartDate());
        goal.setTargetDate(request.getTargetDate());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            goal.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getIcon() != null) goal.setIcon(request.getIcon());
        if (request.getColor() != null) goal.setColor(request.getColor());

        FinancialGoal updated = goalRepository.save(goal);
        return mapToResponse(updated);
    }

    @Transactional
    public GoalResponse updateGoalStatus(Long id, String status) {
        FinancialGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục tiêu tài chính với ID: " + id));

        String upper = status != null ? status.toUpperCase() : "ACTIVE";
        if (!upper.equals("ACTIVE") && !upper.equals("PAUSED") && !upper.equals("COMPLETED") && !upper.equals("CANCELLED")) {
            throw new BadRequestException("Trạng thái mục tiêu không hợp lệ: " + status);
        }

        goal.setStatus(upper);
        FinancialGoal updated = goalRepository.save(goal);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteGoal(Long id) {
        FinancialGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục tiêu tài chính với ID: " + id));

        // Delete contributions first
        List<FinancialGoalContribution> contributions = contributionRepository.findByGoalIdOrderByContributionDateDesc(id);
        if (!contributions.isEmpty()) {
            contributionRepository.deleteAll(contributions);
        }

        goalRepository.delete(goal);
    }

    // ==========================================
    // CONTRIBUTIONS MANAGEMENT
    // ==========================================

    @Transactional(readOnly = true)
    public List<GoalContributionResponse> getContributions(Long goalId) {
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Không tìm thấy mục tiêu tài chính với ID: " + goalId);
        }

        return contributionRepository.findByGoalIdOrderByContributionDateDesc(goalId).stream()
                .map(this::mapContributionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoalContributionResponse addContribution(GoalContributionRequest request) {
        validateContributionRequest(request);

        FinancialGoal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục tiêu tài chính với ID: " + request.getGoalId()));

        FinancialAccount account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + request.getAccountId()));

        String type = request.getType().toUpperCase();
        if ("WITHDRAW".equals(type)) {
            BigDecimal currentNet = contributionRepository.sumNetContributionsForGoal(goal.getId());
            if (currentNet == null || currentNet.compareTo(request.getAmount()) < 0) {
                throw new BadRequestException("Số tiền rút (" + formatVnMoney(request.getAmount()) + ") vượt quá số tiền đã tích lũy của mục tiêu (" + formatVnMoney(currentNet) + ")");
            }
        }

        FinancialGoalContribution contribution = FinancialGoalContribution.builder()
                .goal(goal)
                .account(account)
                .type(type)
                .amount(request.getAmount())
                .contributionDate(request.getContributionDate() != null ? request.getContributionDate() : LocalDateTime.now())
                .note(request.getNote())
                .build();

        FinancialGoalContribution saved = contributionRepository.save(contribution);

        // Auto mark as COMPLETED if target reached and not already completed
        BigDecimal newNet = contributionRepository.sumNetContributionsForGoal(goal.getId());
        if (newNet != null && newNet.compareTo(goal.getTargetAmount()) >= 0 && "ACTIVE".equals(goal.getStatus())) {
            goal.setStatus("COMPLETED");
            goalRepository.save(goal);
        }

        return mapContributionToResponse(saved);
    }

    @Transactional
    public GoalContributionResponse updateContribution(Long contributionId, GoalContributionRequest request) {
        validateContributionRequest(request);

        FinancialGoalContribution contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản đóng góp với ID: " + contributionId));

        FinancialGoal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục tiêu tài chính với ID: " + request.getGoalId()));

        FinancialAccount account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + request.getAccountId()));

        contribution.setGoal(goal);
        contribution.setAccount(account);
        contribution.setType(request.getType().toUpperCase());
        contribution.setAmount(request.getAmount());
        if (request.getContributionDate() != null) contribution.setContributionDate(request.getContributionDate());
        contribution.setNote(request.getNote());

        FinancialGoalContribution updated = contributionRepository.save(contribution);
        return mapContributionToResponse(updated);
    }

    @Transactional
    public void deleteContribution(Long contributionId) {
        FinancialGoalContribution contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản đóng góp với ID: " + contributionId));

        contributionRepository.delete(contribution);
    }

    // ==========================================
    // HELPERS & COMPUTATIONS
    // ==========================================

    private void validateGoalRequest(GoalRequest request) {
        if (request.getTargetAmount() == null || request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền mục tiêu cần lớn hơn 0");
        }
        if (request.getStartDate() != null && request.getTargetDate() != null && request.getTargetDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Hạn hoàn thành không thể trước ngày bắt đầu");
        }
    }

    private void validateContributionRequest(GoalContributionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền đóng góp / rút phải lớn hơn 0");
        }
        String type = request.getType() != null ? request.getType().toUpperCase() : "";
        if (!type.equals("DEPOSIT") && !type.equals("WITHDRAW")) {
            throw new BadRequestException("Loại giao dịch mục tiêu chỉ chấp nhận DEPOSIT (tích lũy) hoặc WITHDRAW (rút tiền)");
        }
    }

    public GoalResponse mapToResponse(FinancialGoal goal) {
        Long id = goal.getId();
        BigDecimal net = contributionRepository.sumNetContributionsForGoal(id);
        BigDecimal currentAmount = net != null && net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO;
        BigDecimal target = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO;

        BigDecimal remainingAmount = target.subtract(currentAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        double progress = 0.0;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            progress = currentAmount.divide(target, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // Monthly savings needed
        BigDecimal monthlySavingsNeeded = null;
        LocalDate today = LocalDate.now();
        if (goal.getTargetDate() != null && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (goal.getTargetDate().isAfter(today)) {
                long daysRemaining = ChronoUnit.DAYS.between(today, goal.getTargetDate());
                double monthsRemaining = Math.max(0.5, daysRemaining / 30.4375);
                monthlySavingsNeeded = remainingAmount.divide(BigDecimal.valueOf(monthsRemaining), 0, RoundingMode.HALF_UP);
            } else {
                monthlySavingsNeeded = remainingAmount; // Đã đến hạn hoặc quá hạn
            }
        }

        // Velocity & Estimated Completion Date
        LocalDate estimatedDate = null;
        String planNote = null;

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            planNote = "Đã hoàn thành mục tiêu!";
        } else {
            // Check contributions in last 3 months
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
            BigDecimal recentContributed = contributionRepository.sumNetContributionsSince(id, threeMonthsAgo);

            if (recentContributed != null && recentContributed.compareTo(BigDecimal.ZERO) > 0) {
                // Average monthly velocity over last 3 months
                BigDecimal monthlyVelocity = recentContributed.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
                if (monthlyVelocity.compareTo(BigDecimal.ZERO) > 0) {
                    double monthsNeeded = remainingAmount.divide(monthlyVelocity, 2, RoundingMode.HALF_UP).doubleValue();
                    estimatedDate = today.plusDays((long) (monthsNeeded * 30.4375));
                    planNote = "Tốc độ trung bình gần đây: " + formatVnMoney(monthlyVelocity) + "/tháng. Dự kiến đạt vào " + estimatedDate.toString();
                }
            }

            if (estimatedDate == null) {
                if (goal.getTargetDate() != null) {
                    if (goal.getTargetDate().isAfter(today)) {
                        planNote = "Cần tích lũy đều đặn " + formatVnMoney(monthlySavingsNeeded) + "/tháng để kịp hạn";
                    } else {
                        planNote = "Đã quá hạn hoàn thành (" + goal.getTargetDate() + ")";
                    }
                } else {
                    planNote = "Chưa thiết lập hạn hoàn thành & chưa có lịch sử tích lũy 3 tháng gần nhất";
                }
            }
        }

        List<FinancialGoalContribution> contributions = contributionRepository.findByGoalIdOrderByContributionDateDesc(id);

        return GoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .targetAmount(target)
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .icon(goal.getIcon())
                .color(goal.getColor())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .currentAmount(currentAmount)
                .remainingAmount(remainingAmount)
                .progressPercentage(progress)
                .monthlySavingsNeeded(monthlySavingsNeeded)
                .estimatedCompletionDate(estimatedDate)
                .planStatusNote(planNote)
                .contributionCount((long) contributions.size())
                .build();
    }

    public GoalContributionResponse mapContributionToResponse(FinancialGoalContribution c) {
        return GoalContributionResponse.builder()
                .id(c.getId())
                .goalId(c.getGoal() != null ? c.getGoal().getId() : null)
                .goalName(c.getGoal() != null ? c.getGoal().getName() : null)
                .accountId(c.getAccount() != null ? c.getAccount().getId() : null)
                .accountName(c.getAccount() != null ? c.getAccount().getName() : null)
                .transactionId(c.getTransaction() != null ? c.getTransaction().getId() : null)
                .type(c.getType())
                .amount(c.getAmount())
                .contributionDate(c.getContributionDate())
                .note(c.getNote())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private String formatVnMoney(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return nf.format(amount.setScale(0, RoundingMode.HALF_UP)) + " ₫";
    }
}
