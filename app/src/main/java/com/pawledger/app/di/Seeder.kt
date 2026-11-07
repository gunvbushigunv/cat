package com.pawledger.app.di

import com.pawledger.app.data.AppDatabase
import com.pawledger.app.data.entity.Account
import com.pawledger.app.data.entity.Category

/**
 * 首次启动数据填充：内置分类与账户。
 */
class Seeder(private val db: AppDatabase) {

    suspend fun seedCategories() {
        val dao = db.ledgerDao()
        if (dao.getAllCategories().isNotEmpty()) return

        val defaults = listOf(
            Category(name = "餐饮", icon = "🍜", color = 0xFFF2B36B, isCustom = false, keywords = "美团,饿了么,肯德基,麦当劳,星巴克,外卖,餐厅,饭店,火锅,奶茶,咖啡,早餐,午餐,晚餐,小吃,面馆"),
            Category(name = "交通", icon = "🚌", color = 0xFFA9C8E6, isCustom = false, keywords = "滴滴,打车,公交,地铁,加油,停车,火车,高铁,机票,飞机,共享单车,出行"),
            Category(name = "购物", icon = "🛍️", color = 0xFFF3C6D0, isCustom = false, keywords = "淘宝,天猫,京东,拼多多,超市,商场,购物,下单,旗舰店,便利店"),
            Category(name = "娱乐", icon = "🎮", color = 0xFFC9A87C, isCustom = false, keywords = "游戏,电影,视频,会员,演出,门票,ktv,健身,音乐"),
            Category(name = "居住", icon = "🏠", color = 0xFF8FB58A, isCustom = false, keywords = "房租,水电,物业,燃气,宽带,房租费"),
            Category(name = "医疗", icon = "💊", color = 0xFFE88B8B, isCustom = false, keywords = "医院,药房,挂号,体检,药店,诊所"),
            Category(name = "教育", icon = "📚", color = 0xFFB0A896, isCustom = false, keywords = "学费,培训,书籍,课程,教育"),
            Category(name = "通讯", icon = "📱", color = 0xFFA9C8E6, isCustom = false, keywords = "话费,流量,充值,宽带"),
            Category(name = "服饰", icon = "👗", color = 0xFFF3C6D0, isCustom = false, keywords = "衣服,鞋,包,饰品,服装"),
            Category(name = "日用", icon = "🧴", color = 0xFFC9A87C, isCustom = false, keywords = "日用,家居,清洁,超市"),
            Category(name = "旅行", icon = "✈️", color = 0xFF8FB58A, isCustom = false, keywords = "酒店,机票,旅游,景点,民宿,度假"),
            Category(name = "宠物", icon = "🐾", color = 0xFFF2B36B, isCustom = false, keywords = "宠物,猫粮,狗粮,宠物店"),
            Category(name = "其它", icon = "📦", color = 0xFFB0A896, isCustom = false, keywords = null)
        )
        defaults.forEach { dao.insertCategory(it) }
    }

    suspend fun seedAccounts() {
        val dao = db.ledgerDao()
        if (dao.getAllAccounts().isNotEmpty()) return
        val defaults = listOf(
            Account(name = "支付宝", type = "ALIPAY", color = 0xFF1677FF),
            Account(name = "微信", type = "WECHAT", color = 0xFF07C160),
            Account(name = "银行卡", type = "BANK", color = 0xFF8B8B8B),
            Account(name = "现金", type = "CASH", color = 0xFFC9A87C)
        )
        defaults.forEach { dao.insertAccount(it) }
    }
}
