/**
 * Utility functions for formatting personal finance numbers and dates
 */

export function formatCurrency(amount: number | null | undefined): string {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '0 ₫';
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatNumber(amount: number | null | undefined): string {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '0';
  }
  return new Intl.NumberFormat('vi-VN').format(Math.round(amount));
}

export function parseCurrencyInput(value: string | number | null | undefined): number {
  if (value === null || value === undefined) return 0;
  if (typeof value === 'number') return isNaN(value) ? 0 : value;
  const clean = value.replace(/[^\d]/g, '');
  const num = parseInt(clean, 10);
  return isNaN(num) ? 0 : num;
}

export function formatCompactCurrency(amount: number | null | undefined): string {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '0 ₫';
  }
  const abs = Math.abs(amount);
  const sign = amount < 0 ? '-' : '';
  if (abs >= 1_000_000_000) {
    const val = parseFloat((abs / 1_000_000_000).toFixed(1));
    return `${sign}${val} Tỷ`;
  }
  if (abs >= 1_000_000) {
    const val = parseFloat((abs / 1_000_000).toFixed(1));
    return `${sign}${val} Tr`;
  }
  if (abs >= 1_000) {
    const val = parseFloat((abs / 1_000).toFixed(0));
    return `${sign}${val} K`;
  }
  return `${sign}${formatNumber(abs)} ₫`;
}

export function formatDate(dateString: string | null | undefined): string {
  if (!dateString) return '-';
  try {
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return dateString;
    return d.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  } catch {
    return dateString;
  }
}

export function formatDateTime(dateString: string | null | undefined): string {
  if (!dateString) return '-';
  try {
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return dateString;
    return d.toLocaleString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  } catch {
    return dateString;
  }
}

export function getAccountTypeLabel(type: string): string {
  switch (type?.toUpperCase()) {
    case 'CASH':
      return 'Tiền mặt';
    case 'BANK':
      return 'Tài khoản Ngân hàng';
    case 'EWALLET':
      return 'Ví điện tử (Momo/ZaloPay)';
    case 'CREDIT':
      return 'Thẻ tín dụng';
    case 'INVESTMENT':
      return 'Tài khoản đầu tư / Tiết kiệm';
    default:
      return 'Khác';
  }
}

export function getGoalStatusLabel(status: string): { label: string; class: string } {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return { label: 'Đang thực hiện', class: 'bg-emerald-50 text-emerald-700 border-emerald-200' };
    case 'PAUSED':
      return { label: 'Tạm dừng', class: 'bg-amber-50 text-amber-700 border-amber-200' };
    case 'COMPLETED':
      return { label: 'Đã hoàn thành', class: 'bg-blue-50 text-blue-700 border-blue-200' };
    case 'CANCELLED':
      return { label: 'Đã hủy', class: 'bg-slate-100 text-slate-600 border-slate-200' };
    default:
      return { label: status || 'Không rõ', class: 'bg-slate-100 text-slate-700 border-slate-200' };
  }
}
