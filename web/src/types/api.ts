// Response wrapper type (matches backend Result<T>)
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T | null
  timestamp?: number
}

// Pagination response
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// DTOs - Data Transfer Objects (for requests)
export interface LoginDTO {
  email: string
  password: string
}

export interface RegisterDTO {
  username: string
  email: string
  password: string
  phone?: string
}

export interface ItemDTO {
  title: string
  description: string
  category: string
  location: string
  lostTime?: string
  foundTime?: string
  images?: string[]
  latitude?: number
  longitude?: number
}

export interface UpdateProfileDTO {
  username?: string
  phone?: string
  avatar?: string
}

export interface ItemSearchDTO {
  keyword?: string
  category?: string
  status?: string
  startDate?: string
  endDate?: string
  current?: number
  size?: number
}

export interface ItemAdminSearchDTO extends ItemSearchDTO {
  userId?: string
}

export interface SendMessageDTO {
  receiverId: string
  content: string
  itemId?: string
}

export interface MatchFeedbackDTO {
  matchId: string
  feedback: 'correct' | 'incorrect'
  comment?: string
}

export interface ConfirmMatchDTO {
  matchId: string
  confirmed: boolean
}

export interface UpdateCategoryDTO {
  category: string
}

// VOs - Value Objects (for responses)
export interface UserVO {
  id: string
  username: string
  email: string
  phone?: string
  avatar?: string
  role: string
  points: number
  createdAt: string
  updatedAt?: string
}

export interface ItemVO {
  id: string
  title: string
  description: string
  category: string
  location: string
  status: 'lost' | 'found' | 'matched' | 'closed'
  lostTime?: string
  foundTime?: string
  images: string[]
  latitude?: number
  longitude?: number
  userId: string
  username?: string
  userAvatar?: string
  createdAt: string
  updatedAt?: string
}

export interface ItemDetailVO extends ItemVO {
  user: UserVO
  matchedItems?: ItemVO[]
}

export interface MessageVO {
  id: string
  senderId: string
  senderName: string
  senderAvatar?: string
  receiverId: string
  receiverName: string
  receiverAvatar?: string
  content: string
  itemId?: string
  itemTitle?: string
  read: boolean
  createdAt: string
}

export interface MatchVO {
  id: string
  lostItemId: string
  lostItemTitle: string
  foundItemId: string
  foundItemTitle: string
  similarity: number
  status: 'pending' | 'confirmed' | 'rejected'
  createdAt: string
  updatedAt?: string
}

export interface TokenVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface PointRecordVO {
  id: string
  userId: string
  points: number
  reason: string
  type: 'earn' | 'spend'
  createdAt: string
}

export interface PointRankVO {
  rank: number
  userId: string
  username: string
  avatar?: string
  points: number
}

export interface StatisticsVO {
  totalItems: number
  lostItems: number
  foundItems: number
  matchedItems: number
  totalUsers: number
  totalMatches: number
  recentMatches: number
}
