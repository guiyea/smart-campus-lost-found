/**
 * 智能校园失物招领平台 - TypeScript 类型定义
 * 
 * 本文件定义了所有前端与后端交互的数据类型，包括：
 * - API 响应包装类型
 * - 分页响应类型
 * - 用户相关类型（DTO/VO）
 * - 物品信息相关类型（DTO/VO）
 * - 匹配相关类型
 * - 消息通知相关类型
 * - 积分相关类型
 * - 管理后台相关类型
 * - 地理位置相关类型
 * - 文件上传相关类型
 */

// ==================== 基础响应类型 ====================

/**
 * API 统一响应包装类型（对应后端 Result<T>）
 */
export interface ApiResponse<T = any> {
  /** 业务状态码 */
  code: number
  /** 提示信息 */
  message: string
  /** 响应数据 */
  data: T | null
  /** 时间戳 */
  timestamp?: number
}

/**
 * 分页响应类型（对应后端 PageResult<T>）
 */
export interface PageResult<T> {
  /** 数据列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 每页大小 */
  size: number
  /** 当前页码 */
  current: number
  /** 总页数 */
  pages: number
}

// ==================== 用户相关类型 ====================

/**
 * 用户注册 DTO
 */
export interface RegisterDTO {
  /** 学号/工号 */
  studentId: string
  /** 姓名 */
  name: string
  /** 手机号 */
  phone: string
  /** 密码 */
  password: string
}

/**
 * 用户登录 DTO
 */
export interface LoginDTO {
  /** 学号/工号 */
  studentId: string
  /** 密码 */
  password: string
}

/**
 * 用户信息更新 DTO
 */
export interface UpdateProfileDTO {
  /** 姓名 */
  name?: string
  /** 手机号 */
  phone?: string
  /** 头像URL */
  avatar?: string
  /** 旧密码 */
  oldPassword?: string
  /** 新密码 */
  newPassword?: string
}

/**
 * 用户信息 VO
 */
export interface UserVO {
  /** 用户ID */
  id: number
  /** 学号/工号 */
  studentId: string
  /** 姓名 */
  name: string
  /** 手机号（脱敏） */
  phone: string
  /** 头像URL */
  avatar?: string
  /** 积分 */
  points: number
  /** 角色：0-普通用户，1-管理员 */
  role: number
  /** 状态：0-正常，1-封禁 */
  status: number
  /** 创建时间 */
  createdAt: string
}

/**
 * 认证令牌 VO
 */
export interface TokenVO {
  /** 访问令牌 */
  accessToken: string
  /** 刷新令牌 */
  refreshToken: string
  /** 访问令牌过期时间（秒） */
  expiresIn: number
  /** 用户信息 */
  userInfo: UserVO
}

// ==================== 物品信息相关类型 ====================

/**
 * 物品信息发布 DTO
 */
export interface ItemDTO {
  /** 标题 */
  title: string
  /** 描述 */
  description: string
  /** 类型：0-失物，1-招领 */
  type: number
  /** 物品类别 */
  category?: string
  /** 图片URL列表 */
  images: string[]
  /** 经度 */
  longitude?: number
  /** 纬度 */
  latitude?: number
  /** 地点描述 */
  locationDesc: string
  /** 丢失/拾获时间 */
  eventTime: string
}

/**
 * 物品搜索 DTO
 */
export interface ItemSearchDTO {
  /** 搜索关键词 */
  keyword?: string
  /** 类型筛选：0-失物，1-招领 */
  type?: number
  /** 类别筛选 */
  category?: string
  /** 状态筛选：0-待处理，1-已找回，2-已关闭 */
  status?: number
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 经度（用于地理范围搜索） */
  longitude?: number
  /** 纬度（用于地理范围搜索） */
  latitude?: number
  /** 搜索半径（米） */
  radius?: number
  /** 排序方式：time-时间，distance-距离，match-匹配度 */
  sortBy?: 'time' | 'distance' | 'match'
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 管理员物品搜索 DTO
 */
export interface ItemAdminSearchDTO extends ItemSearchDTO {
  /** 用户ID筛选 */
  userId?: number
  /** 删除状态筛选：0-未删除，1-已删除 */
  deleted?: number
}

/**
 * 物品信息 VO
 */
export interface ItemVO {
  /** 物品ID */
  id: number
  /** 发布者用户ID */
  userId: number
  /** 发布者姓名 */
  userName: string
  /** 发布者头像 */
  userAvatar?: string
  /** 标题 */
  title: string
  /** 描述 */
  description: string
  /** 类型：0-失物，1-招领 */
  type: number
  /** 物品类别 */
  category: string
  /** 图片URL列表 */
  images: string[]
  /** 标签列表 */
  tags: string[]
  /** 经度 */
  longitude?: number
  /** 纬度 */
  latitude?: number
  /** 地点描述 */
  locationDesc: string
  /** 丢失/拾获时间 */
  eventTime: string
  /** 状态：0-待处理，1-已找回，2-已关闭 */
  status: number
  /** 浏览次数 */
  viewCount: number
  /** 删除状态：0-未删除，1-已删除 */
  deleted?: number
  /** 距离（计算字段，单位：米） */
  distance?: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/**
 * 物品详情 VO
 */
export interface ItemDetailVO extends ItemVO {
  /** 匹配推荐列表 */
  matchRecommendations: ItemVO[]
}

/**
 * 物品类别更新 DTO
 */
export interface UpdateCategoryDTO {
  /** 物品类别 */
  category: string
}

// ==================== 匹配相关类型 ====================

/**
 * 匹配结果
 */
export interface MatchResult {
  /** 物品ID */
  itemId: number
  /** 匹配的物品ID */
  matchedItemId: number
  /** 总匹配分数 */
  score: number
  /** 类别匹配分数 */
  categoryScore: number
  /** 标签匹配分数 */
  tagScore: number
  /** 时间匹配分数 */
  timeScore: number
  /** 位置匹配分数 */
  locationScore: number
}

/**
 * 匹配信息 VO
 */
export interface MatchVO extends ItemVO {
  /** 匹配分数 */
  matchScore: number
}

/**
 * 匹配确认 DTO
 */
export interface ConfirmMatchDTO {
  /** 物品ID */
  itemId: number
  /** 匹配的物品ID */
  matchedItemId: number
}

/**
 * 匹配反馈 DTO
 */
export interface MatchFeedbackDTO {
  /** 物品ID */
  itemId: number
  /** 匹配的物品ID */
  matchedItemId: number
  /** 是否准确 */
  isAccurate: boolean
  /** 评论 */
  comment?: string
}

// ==================== 消息通知相关类型 ====================

/**
 * 发送消息 DTO
 */
export interface SendMessageDTO {
  /** 接收用户ID */
  userId: number
  /** 消息标题 */
  title: string
  /** 消息内容 */
  content: string
  /** 消息类型：0-系统通知，1-匹配通知，2-留言通知 */
  type: number
  /** 关联ID */
  relatedId?: number
}

/**
 * 消息信息 VO
 */
export interface MessageVO {
  /** 消息ID */
  id: number
  /** 消息标题 */
  title: string
  /** 消息内容 */
  content: string
  /** 消息类型：0-系统通知，1-匹配通知，2-留言通知 */
  type: number
  /** 消息类型名称 */
  typeName: string
  /** 关联ID */
  relatedId?: number
  /** 是否已读 */
  isRead: boolean
  /** 创建时间 */
  createdAt: string
}

// ==================== 积分相关类型 ====================

/**
 * 积分记录 VO
 */
export interface PointRecordVO {
  /** 记录ID */
  id: number
  /** 积分变动 */
  points: number
  /** 变动原因 */
  reason: string
  /** 原因描述 */
  reasonDesc: string
  /** 关联ID */
  relatedId?: number
  /** 创建时间 */
  createdAt: string
}

/**
 * 积分排行榜 VO
 */
export interface PointRankVO {
  /** 排名 */
  rank: number
  /** 用户ID */
  userId: number
  /** 用户姓名 */
  userName: string
  /** 用户头像 */
  userAvatar?: string
  /** 积分总数 */
  points: number
}

// ==================== 管理后台相关类型 ====================

/**
 * 数据统计 VO
 */
export interface StatisticsVO {
  /** 用户总数 */
  totalUsers: number
  /** 信息总数 */
  totalItems: number
  /** 失物信息数 */
  totalLostItems: number
  /** 招领信息数 */
  totalFoundItems: number
  /** 匹配成功数 */
  totalMatched: number
  /** 匹配成功率 */
  matchRate: number
  /** 今日新增用户 */
  todayNewUsers: number
  /** 今日新增信息 */
  todayNewItems: number
  /** 近7天数据趋势 */
  weeklyTrend: DailyStatVO[]
}

/**
 * 每日统计 VO
 */
export interface DailyStatVO {
  /** 日期 */
  date: string
  /** 新增用户数 */
  newUsers: number
  /** 新增信息数 */
  newItems: number
  /** 匹配成功数 */
  matchedCount: number
}

/**
 * 分类统计导出 VO
 */
export interface CategoryStatExportVO {
  /** 类别名称 */
  category: string
  /** 失物数量 */
  lostCount: number
  /** 招领数量 */
  foundCount: number
  /** 匹配成功数 */
  matchedCount: number
  /** 匹配成功率 */
  matchRate: number
}

/**
 * 每日统计导出 VO
 */
export interface DailyStatExportVO {
  /** 日期 */
  date: string
  /** 新增用户数 */
  newUsers: number
  /** 新增失物数 */
  newLostItems: number
  /** 新增招领数 */
  newFoundItems: number
  /** 匹配成功数 */
  matchedCount: number
  /** 累计用户数 */
  totalUsers: number
  /** 累计信息数 */
  totalItems: number
}

/**
 * 概览统计导出 VO
 */
export interface OverviewExportVO {
  /** 统计项目 */
  item: string
  /** 数值 */
  value: number
  /** 单位 */
  unit: string
  /** 备注 */
  remark?: string
}

// ==================== 地理位置相关类型 ====================

/**
 * 地理坐标点
 */
export interface GeoPoint {
  /** 经度 */
  longitude: number
  /** 纬度 */
  latitude: number
  /** 地址描述 */
  address?: string
}

// ==================== AI 图像识别相关类型 ====================

/**
 * 图像识别结果
 */
export interface RecognitionResult {
  /** 物品类别 */
  category: string
  /** 置信度 */
  confidence: number
  /** 标签列表 */
  tags: TagInfo[]
  /** 原始响应 */
  rawResponse?: any
}

/**
 * 标签信息
 */
export interface TagInfo {
  /** 标签名称 */
  tag: string
  /** 置信度 */
  confidence: number
}

// ==================== 文件上传相关类型 ====================

/**
 * 文件上传响应
 */
export interface FileUploadResponse {
  /** 文件URL */
  url: string
  /** 文件名 */
  filename: string
  /** 文件大小（字节） */
  size: number
}

/**
 * 批量文件上传响应
 */
export interface BatchFileUploadResponse {
  /** 文件URL列表 */
  urls: string[]
  /** 成功上传数量 */
  successCount: number
  /** 失败上传数量 */
  failCount: number
}

// ==================== 枚举类型 ====================

/**
 * 物品类型；0-失物，1-招领
 */
export type ItemType = 0 | 1

/**
 * 物品状态；0-待处理，1-已找回，2-已关闭
 */
export type ItemStatus = 0 | 1 | 2

/**
 * 用户角色；0-普通用户，1-管理员
 */
export type UserRole = 0 | 1

/**
 * 用户状态；0-正常，1-封禁
 */
export type UserStatus = 0 | 1

/**
 * 消息类型；0-系统通知，1-匹配通知，2-留言通知
 */
export type MessageType = 0 | 1 | 2

/**
 * 匹配记录状态；0-待确认，1-已确认，2-已拒绝
 */
export type MatchStatus = 0 | 1 | 2

/**
 * 积分变动原因
 */
export type PointReason = 'DAILY_LOGIN' | 'PUBLISH_FOUND' | 'HELP_FIND'

/**
 * 排序方式
 */
export type SortBy = 'time' | 'distance' | 'match'

// ==================== 常用类型别名 ====================

/**
 * ID 类型
 */
export type ID = number

/**
 * 时间戳类型
 */
export type Timestamp = string

/**
 * URL 类型
 */
export type URL = string

/**
 * 经纬度坐标类型
 */
export type Coordinate = [number, number] // [longitude, latitude]

/**
 * 分页参数类型
 */
export interface PaginationParams {
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 排序参数类型
 */
export interface SortParams {
  /** 排序字段 */
  sortBy?: string
  /** 排序方向：asc-升序，desc-降序 */
  sortOrder?: 'asc' | 'desc'
}

/**
 * 搜索参数基础类型
 */
export interface BaseSearchParams extends PaginationParams, SortParams {
  /** 搜索关键词 */
  keyword?: string
}

// ==================== 表单验证相关类型 ====================

/**
 * 表单验证规则类型
 */
export interface FormRule {
  /** 是否必填 */
  required?: boolean
  /** 错误提示信息 */
  message?: string
  /** 触发验证的时机 */
  trigger?: 'blur' | 'change'
  /** 最小长度 */
  min?: number
  /** 最大长度 */
  max?: number
  /** 正则表达式 */
  pattern?: RegExp
  /** 自定义验证函数 */
  validator?: (rule: any, value: any, callback: any) => void
}

/**
 * 表单验证规则集合类型
 */
export type FormRules<T = any> = {
  [K in keyof T]?: FormRule[]
}

// ==================== WebSocket 相关类型 ====================

/**
 * WebSocket 消息类型
 */
export interface WebSocketMessage {
  /** 消息类型 */
  type: 'message' | 'match' | 'system'
  /** 消息数据 */
  data: any
  /** 时间戳 */
  timestamp: number
}

/**
 * WebSocket 连接状态
 */
export type WebSocketStatus = 'connecting' | 'connected' | 'disconnected' | 'error'

// 该文件中的类型/枚举已经通过各自的 export 声明导出，无需额外的聚合导出。


