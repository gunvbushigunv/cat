package com.pawledger.app.domain.catalog

import kotlin.random.Random

/**
 * 文案池：鼓励 / 汇报 / 撒娇 / 旅行 / 饿肚子，每类多句、随机不重复。
 */
object Dialogues {

    val encourage = listOf(
        "今天也要元气满满地存钱哦～",
        "离目标又近一步啦，棒！",
        "坚持记账的你，超厉害！",
        "每一笔小小的存款，都会开花结果。",
        "你负责努力，我负责为你加油！",
        "今天的心情，也要像余额一样稳稳上涨呀。",
        "存钱不是苦差事，是给未来的自己攒小惊喜～",
        "慢一点也没关系，我们一直在往前。"
    )

    val coquettish = listOf(
        "摸摸我的头嘛～",
        "想要小鱼干…（眼巴巴）",
        "陪我再玩一会儿好不好～",
        "你回来啦，我一直在这里等你哦。",
        "抱抱我嘛，抱一下就有好心情。"
    )

    val travel = listOf(
        "我出门啦，回来给你带明信片～",
        "我回来啦！带了好多特产！",
        "今天的风，很适合去旅行哦。",
        "路上的风景，我都替你记下了。"
    )

    val hungry = listOf(
        "肚子咕咕叫了…记得给我投喂呀。",
        "有点饿饿的，想啃一口小鱼干。",
        "我好饿，快看看我～"
    )

    val reportTemplates = listOf(
        "本月已存 ¥%s，继续保持！",
        "预算还剩 ¥%s，很稳～",
        "这周存钱进度 %d%%，冲鸭！",
        "你已经连续记账 %d 天啦，好样的！",
        "今日支出 ¥%s，控制在计划内呢。"
    )

    val outOfBudget = listOf(
        "这个月花得有点多，我们一起收紧一点好不好？",
        "预算快见底啦，先抱紧小钱包～"
    )

    private val lastIndex = mutableMapOf<String, Int>()

    /** 随机抽取，且不与上一次重复。 */
    fun pick(pool: List<String>): String {
        if (pool.isEmpty()) return ""
        if (pool.size == 1) return pool[0]
        val key = pool.first() + pool.size
        val last = lastIndex[key]
        var idx = Random.nextInt(pool.size)
        if (last != null && idx == last) {
            idx = (idx + 1) % pool.size
        }
        lastIndex[key] = idx
        return pool[idx]
    }
}
