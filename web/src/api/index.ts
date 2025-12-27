/**
 * API 请求模块
 * 
 * 本模块提供所有与后端 API 交互的方法，包括：
 * - 用户认证相关 API
 * - 物品信息相关 API
 * - 匹配推荐相关 API
 * - 消息通知相关 API
 * - 积分系统相关 API
 * - 文件上传相关 API
 * - 管理后台相关 API
 */

// 导出所有 API 模块
export * from './auth'
export * from './user'
export * from './item'
export * from './match'
export * from './message'
export * from './point'
export * from './file'
export * from './admin'

// 导出请求实例
export { default as request } from '../utils/request'