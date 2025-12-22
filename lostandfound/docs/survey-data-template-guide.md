# 问卷数据收集与分析Excel模板指南

## 一、Excel表格结构设计

### 1.1 数据表（Sheet1: RawData）

建议的列结构：

| 列名 | 字段说明 | 数据类型 | 示例值 |
|------|---------|---------|--------|
| ID | 问卷编号 | 数字 | 1, 2, 3... |
| SubmitTime | 提交时间 | 日期时间 | 2024-12-22 14:30:25 |
| Duration | 答题时长(秒) | 数字 | 180 |
| Q1_Identity | 身份 | 文本 | 本科生 |
| Q2_Gender | 性别 | 文本 | 男 |
| Q3_LostExp | 丢失经历 | 文本 | 偶尔（1-2次） |
| Q4_FoundExp | 捡到经历 | 文本 | 从未捡到过 |
| Q5_Methods_1 | 现有方式-失物招领处 | 0/1 | 1 |
| Q5_Methods_2 | 现有方式-论坛贴吧 | 0/1 | 1 |
| Q5_Methods_3 | 现有方式-微信QQ群 | 0/1 | 0 |
| Q5_Methods_4 | 现有方式-朋友圈 | 0/1 | 1 |
| Q5_Methods_5 | 现有方式-保卫处 | 0/1 | 0 |
| Q5_Methods_Other | 现有方式-其他 | 文本 | 班级群 |
| Q6_Satisfaction | 满意度 | 文本 | 一般 |
| Q7_Problems_1 | 问题-传播范围有限 | 0/1 | 1 |
| Q7_Problems_2 | 问题-更新不及时 | 0/1 | 0 |
| Q7_Problems_3 | 问题-查找不便 | 0/1 | 1 |
| Q7_Problems_4 | 问题-匹配效率低 | 0/1 | 1 |
| Q7_Problems_5 | 问题-缺乏激励 | 0/1 | 0 |
| Q7_Problems_6 | 问题-联系困难 | 0/1 | 1 |
| Q7_Problems_Other | 问题-其他 | 文本 | |
| Q8_SuccessRate | 找回成功率 | 文本 | 20%-50% |
| Q9_UseIntention | 使用意愿 | 文本 | 可能会使用 |
| Q10_BasicFunc_1 | 基本功能-发布信息 | 0/1 | 1 |
| Q10_BasicFunc_2 | 基本功能-搜索浏览 | 0/1 | 1 |
| Q10_BasicFunc_3 | 基本功能-上传图片 | 0/1 | 1 |
| Q10_BasicFunc_4 | 基本功能-标记地点 | 0/1 | 1 |
| Q10_BasicFunc_5 | 基本功能-留言沟通 | 0/1 | 1 |
| Q10_BasicFunc_6 | 基本功能-消息通知 | 0/1 | 1 |
| Q10_BasicFunc_7 | 基本功能-信息管理 | 0/1 | 0 |
| Q10_BasicFunc_Other | 基本功能-其他 | 文本 | |
| Q11_AIDemand | AI功能需求 | 文本 | 比较需要 |
| Q12_LBSDemand | LBS功能需求 | 文本 | 非常需要 |
| Q13_MatchDemand | 匹配功能需求 | 文本 | 非常需要 |
| Q14_MatchFactors_1 | 匹配因素-类别 | 0/1 | 1 |
| Q14_MatchFactors_2 | 匹配因素-特征 | 0/1 | 1 |
| Q14_MatchFactors_3 | 匹配因素-时间 | 0/1 | 1 |
| Q14_MatchFactors_4 | 匹配因素-地点 | 0/1 | 0 |
| Q14_MatchFactors_5 | 匹配因素-价值 | 0/1 | 0 |
| Q14_MatchFactors_Other | 匹配因素-其他 | 文本 | |
| Q15_PointDemand | 积分功能需求 | 文本 | 一般 |
| Q16_PointReward_1 | 积分奖励-找回物品 | 0/1 | 1 |
| Q16_PointReward_2 | 积分奖励-发布招领 | 0/1 | 1 |
| Q16_PointReward_3 | 积分奖励-每日登录 | 0/1 | 0 |
| Q16_PointReward_4 | 积分奖励-完善信息 | 0/1 | 0 |
| Q16_PointReward_5 | 积分奖励-留言互动 | 0/1 | 0 |
| Q16_PointReward_Other | 积分奖励-其他 | 文本 | |
| Q17_NotifyMethod_1 | 通知方式-站内消息 | 0/1 | 1 |
| Q17_NotifyMethod_2 | 通知方式-微信推送 | 0/1 | 1 |
| Q17_NotifyMethod_3 | 通知方式-短信 | 0/1 | 0 |
| Q17_NotifyMethod_4 | 通知方式-邮件 | 0/1 | 0 |
| Q17_NotifyMethod_5 | 通知方式-不需要 | 0/1 | 0 |
| Q18_PublicInfo_1 | 公开信息-姓名 | 0/1 | 0 |
| Q18_PublicInfo_2 | 公开信息-学号工号 | 0/1 | 0 |
| Q18_PublicInfo_3 | 公开信息-手机号 | 0/1 | 1 |
| Q18_PublicInfo_4 | 公开信息-头像 | 0/1 | 1 |
| Q18_PublicInfo_5 | 公开信息-积分排名 | 0/1 | 1 |
| Q18_PublicInfo_6 | 公开信息-匹配后公开 | 0/1 | 1 |
| Q18_PublicInfo_Other | 公开信息-其他 | 文本 | |
| Q19_PrivacyConcern | 隐私重视程度 | 文本 | 非常重视 |
| Q20_Security_1 | 安全措施-实名认证 | 0/1 | 1 |
| Q20_Security_2 | 安全措施-手机验证 | 0/1 | 1 |
| Q20_Security_3 | 安全措施-信息审核 | 0/1 | 1 |
| Q20_Security_4 | 安全措施-举报功能 | 0/1 | 1 |
| Q20_Security_5 | 安全措施-信用评级 | 0/1 | 0 |
| Q20_Security_Other | 安全措施-其他 | 文本 | |
| Q21_PlatformType | 平台形式偏好 | 文本 | 微信小程序 |
| Q22_UIImportance_1 | UI重要性-简洁美观 | 0/1 | 1 |
| Q22_UIImportance_2 | UI重要性-操作便捷 | 0/1 | 1 |
| Q22_UIImportance_3 | UI重要性-功能齐全 | 0/1 | 0 |
| Q22_UIImportance_4 | UI重要性-响应快 | 0/1 | 1 |
| Q22_UIImportance_5 | UI重要性-展示清晰 | 0/1 | 0 |
| Q22_UIImportance_Other | UI重要性-其他 | 文本 | |
| Q23_PayWillingness | 付费意愿 | 文本 | 希望完全免费 |
| Q24_Recommend | 推荐意愿 | 文本 | 可能会推荐 |
| Q25_Value_1 | 平台价值-提高成功率 | 0/1 | 1 |
| Q25_Value_2 | 平台价值-节省时间 | 0/1 | 1 |
| Q25_Value_3 | 平台价值-方便查询 | 0/1 | 1 |
| Q25_Value_4 | 平台价值-互助氛围 | 0/1 | 0 |
| Q25_Value_5 | 平台价值-减少损失 | 0/1 | 0 |
| Q25_Value_Other | 平台价值-其他 | 文本 | |
| Q26_OtherFunctions | 其他功能建议 | 长文本 | 希望能够... |
| Q27_Suggestions | 建议和期望 | 长文本 | 建议增加... |
| Q28_Experience | 个人经历 | 长文本 | 我曾经在... |
| IsValid | 是否有效 | 0/1 | 1 |
| Notes | 备注 | 文本 | |

### 1.2 编码表（Sheet2: CodeBook）

| 题号 | 变量名 | 变量类型 | 编码规则 |
|------|--------|---------|---------|
| Q1 | Q1_Identity | 分类 | 1=本科生, 2=研究生, 3=教职工, 4=其他 |
| Q2 | Q2_Gender | 分类 | 1=男, 2=女, 3=不便透露 |
| Q3 | Q3_LostExp | 有序 | 1=经常, 2=偶尔, 3=从未, 4=不记得 |
| Q4 | Q4_FoundExp | 有序 | 1=经常, 2=偶尔, 3=从未, 4=不记得 |
| Q5 | Q5_Methods | 多选 | 1=选中, 0=未选 |
| Q6 | Q6_Satisfaction | 有序 | 1=非常满意, 2=比较满意, 3=一般, 4=不太满意, 5=非常不满意 |
| Q7 | Q7_Problems | 多选 | 1=选中, 0=未选 |
| Q8 | Q8_SuccessRate | 有序 | 1=80%以上, 2=50-80%, 3=20-50%, 4=20%以下, 5=从未 |
| Q9 | Q9_UseIntention | 有序 | 1=一定会, 2=可能会, 3=不确定, 4=可能不会, 5=一定不会 |
| Q10 | Q10_BasicFunc | 多选 | 1=选中, 0=未选 |
| Q11 | Q11_AIDemand | 有序 | 1=非常需要, 2=比较需要, 3=一般, 4=不太需要, 5=完全不需要 |
| Q12 | Q12_LBSDemand | 有序 | 1=非常需要, 2=比较需要, 3=一般, 4=不太需要, 5=完全不需要 |
| Q13 | Q13_MatchDemand | 有序 | 1=非常需要, 2=比较需要, 3=一般, 4=不太需要, 5=完全不需要 |
| Q14 | Q14_MatchFactors | 多选 | 1=选中, 0=未选 |
| Q15 | Q15_PointDemand | 有序 | 1=非常需要, 2=比较需要, 3=一般, 4=不太需要, 5=完全不需要 |
| Q16 | Q16_PointReward | 多选 | 1=选中, 0=未选 |
| Q17 | Q17_NotifyMethod | 多选 | 1=选中, 0=未选 |
| Q18 | Q18_PublicInfo | 多选 | 1=选中, 0=未选 |
| Q19 | Q19_PrivacyConcern | 有序 | 1=非常重视, 2=比较重视, 3=一般, 4=不太重视, 5=完全不重视 |
| Q20 | Q20_Security | 多选 | 1=选中, 0=未选 |
| Q21 | Q21_PlatformType | 分类 | 1=网页版, 2=小程序, 3=APP, 4=都可以 |
| Q22 | Q22_UIImportance | 多选 | 1=选中, 0=未选 |
| Q23 | Q23_PayWillingness | 有序 | 1=愿意, 2=少量费用, 3=完全免费, 4=不确定 |
| Q24 | Q24_Recommend | 有序 | 1=一定会, 2=可能会, 3=不确定, 4=可能不会, 5=一定不会 |
| Q25 | Q25_Value | 多选 | 1=选中, 0=未选 |
| Q26-28 | 开放题 | 文本 | 原文录入 |

### 1.3 统计分析表（Sheet3: Statistics）

#### 基本信息统计

```
【受访者身份分布】
身份          人数    百分比
本科生        130     65.0%
研究生        46      23.0%
教职工        24      12.0%
合计          200     100.0%

【性别分布】
性别          人数    百分比
男            98      49.0%
女            102     51.0%
合计          200     100.0%

【丢失物品经历】
经历          人数    百分比
经常          30      15.0%
偶尔          116     58.0%
从未          54      27.0%
合计          200     100.0%
```

#### 功能需求统计

```
【核心功能需求度排序】
功能              非常需要  比较需要  一般   不需要  需求度*
智能匹配推荐      68.0%    24.0%    6.0%   2.0%    92.0%
消息通知提醒      62.0%    26.0%    9.0%   3.0%    88.0%
地图定位功能      58.0%    27.0%    12.0%  3.0%    85.0%
AI图像识别        54.0%    28.0%    14.0%  4.0%    82.0%
积分激励体系      46.0%    30.0%    18.0%  6.0%    76.0%

*需求度 = 非常需要 + 比较需要
```

### 1.4 图表汇总（Sheet4: Charts）

建议创建的图表：
1. 受访者身份分布饼图
2. 现有方式满意度柱状图
3. 现有方式问题统计（横向条形图）
4. 功能需求热力图
5. 使用意愿分布图
6. 平台价值认知雷达图

---

## 二、Excel公式示例

### 2.1 频数统计

```excel
=COUNTIF(Q1_Identity列, "本科生")
```

### 2.2 百分比计算

```excel
=COUNTIF(Q1_Identity列, "本科生") / COUNTA(Q1_Identity列)
```

### 2.3 需求度计算

```excel
=(COUNTIF(Q11_AIDemand列, "非常需要") + COUNTIF(Q11_AIDemand列, "比较需要")) / COUNTA(Q11_AIDemand列)
```

### 2.4 多选题统计

```excel
=SUM(Q5_Methods_1列)  // 统计选择该选项的人数
=SUM(Q5_Methods_1列) / COUNTA(ID列)  // 计算选择率
```

### 2.5 交叉分析（数据透视表）

**步骤：**
1. 选择数据区域
2. 插入 → 数据透视表
3. 行：Q1_Identity
4. 列：Q11_AIDemand
5. 值：计数

---

## 三、数据清洗检查清单

### 3.1 完整性检查

- [ ] 所有必填题是否都有答案
- [ ] 是否存在大量空白行
- [ ] 开放题回答是否有效

### 3.2 一致性检查

- [ ] 答题时长是否合理（建议≥120秒）
- [ ] 多选题选项数是否符合限制
- [ ] 逻辑跳转是否正确

### 3.3 异常值检查

- [ ] 是否存在明显的随意作答（如全选A）
- [ ] 开放题是否有无意义内容（如"无"、"不知道"）
- [ ] 是否存在重复提交

### 3.4 标记无效问卷

在 `IsValid` 列标记：
- 1 = 有效
- 0 = 无效（答题时长<120秒、明显随意作答等）

---

## 四、SPSS数据导入格式

### 4.1 变量视图设置

| 名称 | 类型 | 宽度 | 小数位 | 标签 | 值 | 缺失 | 列 | 对齐 | 测量 |
|------|------|------|--------|------|----|----|----|----|------|
| ID | 数值 | 8 | 0 | 问卷编号 | 无 | 无 | 8 | 右 | 标度 |
| Q1 | 数值 | 8 | 0 | 身份 | 1=本科生, 2=研究生... | 无 | 8 | 右 | 名义 |
| Q2 | 数值 | 8 | 0 | 性别 | 1=男, 2=女... | 无 | 8 | 右 | 名义 |
| Q6 | 数值 | 8 | 0 | 满意度 | 1=非常满意... | 无 | 8 | 右 | 有序 |
| Q11 | 数值 | 8 | 0 | AI需求 | 1=非常需要... | 无 | 8 | 右 | 有序 |

### 4.2 数据视图格式

将文本选项转换为数值编码：

| ID | Q1 | Q2 | Q3 | Q6 | Q11 | Q12 | Q13 |
|----|----|----|----|----|-----|-----|-----|
| 1  | 1  | 1  | 2  | 3  | 2   | 1   | 1   |
| 2  | 1  | 2  | 2  | 4  | 1   | 2   | 1   |
| 3  | 2  | 1  | 1  | 3  | 2   | 1   | 2   |

---

## 五、Python数据分析代码模板

### 5.1 数据读取与清洗

```python
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

# 读取数据
df = pd.read_excel('survey_data.xlsx', sheet_name='RawData')

# 查看基本信息
print(df.info())
print(df.describe())

# 筛选有效问卷
df_valid = df[df['IsValid'] == 1]
print(f"有效问卷数量: {len(df_valid)}")

# 检查缺失值
print(df_valid.isnull().sum())
```

### 5.2 描述性统计

```python
# 身份分布
identity_counts = df_valid['Q1_Identity'].value_counts()
print("身份分布:")
print(identity_counts)
print(identity_counts / len(df_valid) * 100)

# 满意度统计
satisfaction = df_valid['Q6_Satisfaction'].value_counts()
print("\n满意度分布:")
print(satisfaction)

# 功能需求度计算
def calculate_demand(column):
    demand = df_valid[column].isin(['非常需要', '比较需要']).sum()
    return round(demand / len(df_valid) * 100, 1)

features = {
    'AI图像识别': 'Q11_AIDemand',
    'LBS定位': 'Q12_LBSDemand',
    '智能匹配': 'Q13_MatchDemand',
    '积分激励': 'Q15_PointDemand'
}

demand_scores = {name: calculate_demand(col) for name, col in features.items()}
print("\n功能需求度:")
for name, score in sorted(demand_scores.items(), key=lambda x: x[1], reverse=True):
    print(f"{name}: {score}%")
```

### 5.3 可视化

```python
# 1. 身份分布饼图
plt.figure(figsize=(8, 6))
identity_counts.plot(kind='pie', autopct='%1.1f%%')
plt.title('受访者身份分布')
plt.ylabel('')
plt.savefig('identity_distribution.png', dpi=300, bbox_inches='tight')
plt.show()

# 2. 满意度柱状图
plt.figure(figsize=(10, 6))
satisfaction.plot(kind='bar')
plt.title('现有失物招领方式满意度')
plt.xlabel('满意度')
plt.ylabel('人数')
plt.xticks(rotation=45)
plt.savefig('satisfaction.png', dpi=300, bbox_inches='tight')
plt.show()

# 3. 功能需求对比图
plt.figure(figsize=(10, 6))
demand_df = pd.DataFrame(list(demand_scores.items()), columns=['功能', '需求度'])
demand_df = demand_df.sort_values('需求度', ascending=True)
plt.barh(demand_df['功能'], demand_df['需求度'])
plt.xlabel('需求度 (%)')
plt.title('核心功能需求度对比')
plt.savefig('feature_demand.png', dpi=300, bbox_inches='tight')
plt.show()

# 4. 多选题统计（现有方式问题）
problem_cols = [col for col in df_valid.columns if col.startswith('Q7_Problems_') and col != 'Q7_Problems_Other']
problem_counts = df_valid[problem_cols].sum().sort_values(ascending=True)
problem_labels = ['传播范围有限', '更新不及时', '查找不便', '匹配效率低', '缺乏激励', '联系困难']

plt.figure(figsize=(10, 6))
plt.barh(problem_labels, problem_counts.values)
plt.xlabel('提及人数')
plt.title('现有失物招领方式存在的问题')
plt.savefig('problems.png', dpi=300, bbox_inches='tight')
plt.show()
```

### 5.4 交叉分析

```python
# 身份 × AI功能需求
cross_tab = pd.crosstab(df_valid['Q1_Identity'], df_valid['Q11_AIDemand'])
print("\n身份 × AI功能需求交叉表:")
print(cross_tab)

# 百分比交叉表
cross_pct = pd.crosstab(df_valid['Q1_Identity'], df_valid['Q11_AIDemand'], normalize='index') * 100
print("\n百分比交叉表:")
print(cross_pct.round(1))

# 热力图
plt.figure(figsize=(10, 6))
sns.heatmap(cross_pct, annot=True, fmt='.1f', cmap='YlOrRd')
plt.title('不同身份群体对AI功能的需求程度')
plt.xlabel('需求程度')
plt.ylabel('身份')
plt.savefig('cross_analysis.png', dpi=300, bbox_inches='tight')
plt.show()

# 卡方检验
from scipy.stats import chi2_contingency
chi2, p_value, dof, expected = chi2_contingency(cross_tab)
print(f"\n卡方检验结果: χ²={chi2:.4f}, p={p_value:.4f}")
if p_value < 0.05:
    print("结论: 不同身份群体对AI功能需求存在显著差异")
else:
    print("结论: 不同身份群体对AI功能需求无显著差异")
```

### 5.5 开放题分析

```python
import jieba
from wordcloud import WordCloud

# 提取开放题文本
suggestions = df_valid['Q27_Suggestions'].dropna()
all_text = ' '.join(suggestions)

# 分词
words = jieba.cut(all_text)
word_list = [w for w in words if len(w) > 1]  # 过滤单字

# 词频统计
from collections import Counter
word_freq = Counter(word_list)
print("\n高频词汇 Top 20:")
for word, freq in word_freq.most_common(20):
    print(f"{word}: {freq}")

# 生成词云
wordcloud = WordCloud(
    font_path='simhei.ttf',  # 中文字体路径
    width=800,
    height=600,
    background_color='white'
).generate(all_text)

plt.figure(figsize=(12, 8))
plt.imshow(wordcloud, interpolation='bilinear')
plt.axis('off')
plt.title('用户建议关键词云')
plt.savefig('wordcloud.png', dpi=300, bbox_inches='tight')
plt.show()
```

---

## 六、论文图表规范

### 6.1 表格格式

- 使用三线表（顶线、栏目线、底线）
- 表题在表格上方，居中
- 表注在表格下方，左对齐
- 数据右对齐，文字左对齐

### 6.2 图片格式

- 分辨率：≥300 DPI
- 格式：PNG或TIFF
- 图题在图片下方，居中
- 图注在图题下方，左对齐

### 6.3 编号规则

- 表格：表X-1, 表X-2...
- 图片：图X-1, 图X-2...
- X为章节号

---

## 七、常用Excel快捷键

| 快捷键 | 功能 |
|--------|------|
| Ctrl + T | 创建表格 |
| Alt + N + V | 插入数据透视表 |
| Ctrl + Shift + L | 添加筛选器 |
| F2 | 编辑单元格 |
| Ctrl + D | 向下填充 |
| Ctrl + R | 向右填充 |
| Alt + = | 自动求和 |
| Ctrl + 1 | 设置单元格格式 |

---

**文档版本**：v1.0  
**最后更新**：2024年12月  
**适用软件**：Excel 2016及以上版本、SPSS 25及以上版本、Python 3.8及以上版本
