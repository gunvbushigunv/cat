package com.pawledger.app.domain.catalog

import com.pawledger.app.data.entity.PetSpecies

/**
 * 游戏静态目录：伙伴、皮肤、家具、旅行目的地。
 * 动态状态（是否解锁、拥有哪些）存 Room；静态文案与价格在此统一定义。
 */
object Catalog {

    data class SpeciesInfo(
        val species: PetSpecies,
        val defaultName: String,
        val description: String,
        val unlockCost: Int,
        val emoji: String
    )

    data class SkinInfo(
        val key: String,
        val species: PetSpecies,
        val name: String,
        val description: String,
        val price: Int,
        val isLimited: Boolean,
        val emoji: String
    )

    data class FurnitureInfo(
        val key: String,
        val name: String,
        val description: String,
        val price: Int,
        val emoji: String
    )

    data class DestinationInfo(
        val key: String,
        val name: String,
        val emoji: String,
        /** null 表示公共目的地 */
        val species: PetSpecies? = null
    )

    // ---- 伙伴本体 ----
    val species = listOf(
        SpeciesInfo(PetSpecies.CAT, "团子", "一只圆滚滚的小橘猫，最爱晒太阳，也最爱数你的每一笔存款。", 0, "🐱"),
        SpeciesInfo(PetSpecies.DOG, "豆包", "暖乎乎的柴犬，会为你的每一次省钱开心地摇尾巴。", 100, "🐶"),
        SpeciesInfo(PetSpecies.FOX, "小狐", "机灵的小狐狸，蓬松尾巴里藏着悄悄存下的小秘密。", 100, "🦊")
    )

    fun speciesOf(s: PetSpecies): SpeciesInfo = species.first { it.species == s }

    // ---- 皮肤（各 5 款，末位为限定款） ----
    val skins = listOf(
        // 猫
        SkinInfo("cat_base", PetSpecies.CAT, "奶橘", "奶油白配暖橘斑块，团子最经典的模样。", 0, false, "🐱"),
        SkinInfo("cat_calico", PetSpecies.CAT, "三花喵", "白橘黑三色交织，像打翻了的秋日调色盘。", 120, false, "🐈"),
        SkinInfo("cat_cow", PetSpecies.CAT, "奶牛喵", "黑白奶牛纹，看着就想给它喂点牛奶。", 120, false, "🐄"),
        SkinInfo("cat_ragdoll", PetSpecies.CAT, "布偶喵", "蓝眼睛的长毛小公主，软绵绵像一团云。", 150, false, "🐱"),
        SkinInfo("cat_star", PetSpecies.CAT, "星空喵", "披着夜空的猫，尾巴尖上挂着一颗会眨眼的星星。", 220, true, "🌌"),
        // 狗
        SkinInfo("dog_base", PetSpecies.DOG, "赤柴", "暖黄色的柴犬，笑容治愈，最爱陪你散步。", 0, false, "🐶"),
        SkinInfo("dog_corgi", PetSpecies.DOG, "柯基", "短腿圆臀的小柯基，走起路来一扭一扭。", 120, false, "🐕"),
        SkinInfo("dog_samoyed", PetSpecies.DOG, "萨摩耶", "纯白蓬松的萨摩耶，笑起来像冬天第一场雪。", 150, false, "🤍"),
        SkinInfo("dog_border", PetSpecies.DOG, "边牧", "聪明帅气的黑白边牧，你的存款它都算得清清楚楚。", 150, false, "🐾"),
        SkinInfo("dog_cloud", PetSpecies.DOG, "云朵犬", "从云朵里蹦出来的小狗，软乎乎像棉花糖。", 220, true, "☁️"),
        // 狐狸
        SkinInfo("fox_base", PetSpecies.FOX, "赤狐", "橙红色的小狐狸，眼神里藏着机灵的小算盘。", 0, false, "🦊"),
        SkinInfo("fox_snow", PetSpecies.FOX, "雪狐", "纯白如雪的小狐狸，会安静地听你数钱。", 120, false, "❄️"),
        SkinInfo("fox_sakura", PetSpecies.FOX, "樱花狐", "粉色花瓣落在肩头，是春天寄来的一封信。", 150, false, "🌸"),
        SkinInfo("fox_forest", PetSpecies.FOX, "森林狐", "苔绿与森色，是山间小精灵的模样。", 150, false, "🌿"),
        SkinInfo("fox_moon", PetSpecies.FOX, "星月狐", "夜空与月亮，会悄悄帮你把心愿存起来。", 220, true, "🌙")
    )

    fun skinsOf(s: PetSpecies): List<SkinInfo> = skins.filter { it.species == s }
    fun skin(key: String): SkinInfo = skins.first { it.key == key }

    // ---- 家具 ----
    val furniture = listOf(
        FurnitureInfo("fur_yarn", "毛线球", "滚来滚去的快乐，就是这么简单。", 40, "🧶"),
        FurnitureInfo("fur_lantern", "暖黄小灯笼", "天黑也不怕，暖暖的光陪你记账。", 50, "🏮"),
        FurnitureInfo("fur_plant", "苔绿盆栽", "每天浇一点水，心情也会变好哦。", 50, "🪴"),
        FurnitureInfo("fur_tea", "和风茶壶", "一杯热茶，等你回家。", 60, "🍵"),
        FurnitureInfo("fur_rug", "草地小地毯", "踩上去，像走在春天的草地上。", 60, "🟢"),
        FurnitureInfo("fur_books", "小小书架", "连小猫小狗都爱读的童话。", 70, "📚"),
        FurnitureInfo("fur_window", "星星小窗", "晚上透过它，能看到最亮的星。", 80, "⭐"),
        FurnitureInfo("fur_blanket", "软乎乎的毯子", "打盹首选，软到不想起床。", 80, "🧶"),
        FurnitureInfo("fur_bed", "云朵小床", "一躺上去，梦都是软绵绵的。", 100, "🛏️"),
        FurnitureInfo("fur_mailbox", "旅行信箱", "装得下每一张寄回家的明信片。", 100, "📮")
    )

    fun furniture(key: String): FurnitureInfo = furniture.first { it.key == key }

    // ---- 旅行目的地 ----
    val commonDestinations = listOf(
        DestinationInfo("sea", "海边", "🏖️"),
        DestinationInfo("forest", "森林", "🌲"),
        DestinationInfo("snow", "雪山", "🏔️"),
        DestinationInfo("town", "小镇", "🏘️"),
        DestinationInfo("cafe", "咖啡馆", "☕"),
        DestinationInfo("hotspring", "温泉", "♨️"),
        DestinationInfo("flower_field", "花田", "🌼"),
        DestinationInfo("lake", "湖边", "🛶")
    )

    val exclusiveDestinations = mapOf(
        PetSpecies.CAT to listOf(
            DestinationInfo("fish_market", "鱼市场", "🐟", PetSpecies.CAT),
            DestinationInfo("rooftop", "屋顶漫步", "🌇", PetSpecies.CAT),
            DestinationInfo("yarn_shop", "毛线球小铺", "🧶", PetSpecies.CAT)
        ),
        PetSpecies.DOG to listOf(
            DestinationInfo("dog_park", "狗狗公园", "🐕", PetSpecies.DOG),
            DestinationInfo("bone_shop", "骨头商店", "🦴", PetSpecies.DOG),
            DestinationInfo("lakeside", "湖边散步", "🐾", PetSpecies.DOG)
        ),
        PetSpecies.FOX to listOf(
            DestinationInfo("shrine", "稻荷神社", "⛩️", PetSpecies.FOX),
            DestinationInfo("maple_forest", "枫叶林", "🍁", PetSpecies.FOX),
            DestinationInfo("tea_house", "山间茶屋", "🍵", PetSpecies.FOX)
        )
    )

    fun destination(key: String): DestinationInfo =
        (commonDestinations + exclusiveDestinations.values.flatten())
            .first { it.key == key }
}
