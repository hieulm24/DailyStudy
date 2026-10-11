export interface AccountCategory {
  id: number;
  code: string;
  name: string;
  icon?: string;
  color?: string;
  sortOrder: number;
  isSystem: boolean;
  createdAt: string;
}

export interface AccountCredential {
  id: number;
  categoryId?: number;
  categoryName?: string;
  categoryCode?: string;
  categoryColor?: string;
  categoryIcon?: string;

  title: string;
  serviceName?: string;
  websiteUrl?: string;
  appPackageName?: string;

  username?: string;
  email?: string;
  phoneNumber?: string;
  password?: string;
  recoveryEmail?: string;
  linkedCredentialId?: number;
  linkedCredentialTitle?: string;

  pinCode?: string;
  passwordStrength?: 'WEAK' | 'MEDIUM' | 'STRONG' | 'VERY_STRONG';
  passwordChangedAt?: string;
  lastAccessedAt?: string;

  isFavorite: boolean;
  isDeleted: boolean;
  deletedAt?: string;

  attachmentCount: number;
  passwordHistoryCount: number;
  hasTotp: boolean;
  hasNotes: boolean;

  createdAt: string;
  updatedAt: string;
}

export interface AccountAttachment {
  id: number;
  credentialId: number;
  fileName: string;
  storedFileName: string;
  filePath: string;
  fileType?: string;
  mimeType?: string;
  fileSize: number;
  description?: string;
  downloadCount: number;
  createdAt: string;
}

export interface AccountPasswordHistory {
  id: number;
  credentialId: number;
  oldPassword: string;
  changedReason?: string;
  changedAt: string;
}

export interface AccountCredentialDetail extends AccountCredential {
  securityQuestions?: string;
  totpSecret?: string;
  recoveryCodes?: string;
  customFields?: string; // JSON string
  notes?: string;
  attachments: AccountAttachment[];
  passwordHistories: AccountPasswordHistory[];
}

export interface CreateAccountCredentialRequest {
  categoryId?: number;
  title: string;
  serviceName?: string;
  websiteUrl?: string;
  appPackageName?: string;
  username?: string;
  email?: string;
  phoneNumber?: string;
  password: string;
  recoveryEmail?: string;
  linkedCredentialId?: number;
  pinCode?: string;
  securityQuestions?: string;
  totpSecret?: string;
  recoveryCodes?: string;
  customFields?: string;
  notes?: string;
  isFavorite?: boolean;
}

export interface UpdateAccountCredentialRequest extends CreateAccountCredentialRequest {
  passwordChangeReason?: string;
}

export interface CategoryCount {
  categoryId?: number;
  categoryCode: string;
  categoryName: string;
  icon?: string;
  color?: string;
  count: number;
}

export interface VaultSummary {
  totalAccounts: number;
  favoriteCount: number;
  weakPasswordCount: number;
  mediumPasswordCount: number;
  strongPasswordCount: number;
  trashCount: number;
  totalAttachments: number;
  categoryBreakdowns: CategoryCount[];
}

export interface PasswordGeneratorRequest {
  length: number;
  includeUppercase: boolean;
  includeLowercase: boolean;
  includeNumbers: boolean;
  includeSymbols: boolean;
  excludeAmbiguous: boolean;
}

export interface PasswordGeneratorResponse {
  password: string;
  strength: 'WEAK' | 'MEDIUM' | 'STRONG' | 'VERY_STRONG';
  score: number;
}
