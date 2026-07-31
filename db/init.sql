-- ============================================
-- 背单词网站 learnfl 数据库初始化脚本
-- 层级：用户 → 语言 → 词库组（一本书）→ 词库（一课）→ 数据（句子/单词）
-- ============================================

DROP DATABASE IF EXISTS `learnfl`;
CREATE DATABASE `learnfl` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `learnfl`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`   VARCHAR(50)  NOT NULL COMMENT '登录名',
  `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 散列',
  `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `active_language_id` BIGINT DEFAULT NULL COMMENT '当前选中的语言',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '用户';

-- ----------------------------
-- 语言（英语、日语）
-- ----------------------------
DROP TABLE IF EXISTS `language`;
CREATE TABLE `language` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(50) NOT NULL COMMENT '英语/日语',
  `code`       VARCHAR(10) NOT NULL COMMENT 'en / ja',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB COMMENT = '语言';

-- ----------------------------
-- 词库组（一本书，如：小学一年级的英语课本）
-- ----------------------------
DROP TABLE IF EXISTS `word_group`;
CREATE TABLE `word_group` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT,
  `language_id` BIGINT NOT NULL,
  `name`        VARCHAR(100) NOT NULL COMMENT '如：小学一年级的英语课本',
  `description` VARCHAR(500) DEFAULT NULL,
  `sort_order`  INT NOT NULL DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_language` (`language_id`),
  CONSTRAINT `fk_wg_language` FOREIGN KEY (`language_id`) REFERENCES `language`(`id`)
) ENGINE = InnoDB COMMENT = '词库组（一本书）';

-- ----------------------------
-- 词库（书中的一课，如：第一课）
-- ----------------------------
DROP TABLE IF EXISTS `word_bank`;
CREATE TABLE `word_bank` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT,
  `group_id`    BIGINT NOT NULL,
  `name`        VARCHAR(100) NOT NULL COMMENT '如：第一课',
  `description` VARCHAR(500) DEFAULT NULL,
  `sort_order`  INT NOT NULL DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`),
  CONSTRAINT `fk_wb_group` FOREIGN KEY (`group_id`) REFERENCES `word_group`(`id`)
) ENGINE = InnoDB COMMENT = '词库（一课）';

-- ----------------------------
-- 句子（修正：word_bank_id 指向词库而非词库组）
-- ----------------------------
DROP TABLE IF EXISTS `sentence`;
CREATE TABLE `sentence` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word_bank_id`  BIGINT NOT NULL COMMENT '所属词库（修正：原 group_id 改为指向词库）',
  `chinese`       TEXT COMMENT '中文',
  `english`       TEXT COMMENT '英文',
  `japanese`      TEXT COMMENT '日文',
  `sentence_type` VARCHAR(20) DEFAULT NULL COMMENT '如 sentence/dialogue',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bank` (`word_bank_id`),
  CONSTRAINT `fk_sentence_bank` FOREIGN KEY (`word_bank_id`) REFERENCES `word_bank`(`id`)
) ENGINE = InnoDB COMMENT = '句子';

-- ----------------------------
-- 单词
-- ----------------------------
DROP TABLE IF EXISTS `word`;
CREATE TABLE `word` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `word_bank_id` BIGINT NOT NULL,
  `word`         VARCHAR(200) NOT NULL COMMENT '单词本身',
  `phonetic`     VARCHAR(200) DEFAULT NULL COMMENT '音标，如 /ˈæpl/',
  `meaning`      VARCHAR(500) DEFAULT NULL COMMENT '中文释义',
  `word_type`    VARCHAR(20)  DEFAULT NULL COMMENT 'n./v. 等',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bank` (`word_bank_id`),
  CONSTRAINT `fk_word_bank` FOREIGN KEY (`word_bank_id`) REFERENCES `word_bank`(`id`)
) ENGINE = InnoDB COMMENT = '单词';

-- ----------------------------
-- 用户学习进度（每 词库 x 模式 一条）
-- ----------------------------
DROP TABLE IF EXISTS `user_progress`;
CREATE TABLE `user_progress` (
  `id`               BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`          BIGINT NOT NULL,
  `word_bank_id`     BIGINT NOT NULL,
  `mode`             VARCHAR(10) NOT NULL COMMENT 'word / sentence',
  `total_count`      INT NOT NULL DEFAULT 0,
  `completed_count`  INT NOT NULL DEFAULT 0 COMMENT '已完成数',
  `status`           VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS / COMPLETED',
  `last_word_index`  INT NOT NULL DEFAULT 0,
  `completed_at`     DATETIME DEFAULT NULL COMMENT '首次完成时间',
  `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_bank_mode` (`user_id`, `word_bank_id`, `mode`)
) ENGINE = InnoDB COMMENT = '用户学习进度';

-- ----------------------------
-- 练习记录（一次会话，统计来源）
-- ----------------------------
DROP TABLE IF EXISTS `practice_record`;
CREATE TABLE `practice_record` (
  `id`                   BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`              BIGINT NOT NULL,
  `word_bank_id`         BIGINT NOT NULL,
  `mode`                 VARCHAR(10) NOT NULL COMMENT 'word / sentence',
  `order_type`           VARCHAR(10) NOT NULL DEFAULT 'asc' COMMENT 'asc / shuffle',
  `total_words`          INT NOT NULL,
  `correct_first_words`  INT NOT NULL DEFAULT 0 COMMENT '一次打对的词数（默写计分口径）',
  `error_count`          INT NOT NULL DEFAULT 0 COMMENT '错误按键次数',
  `total_keystrokes`     INT NOT NULL DEFAULT 0,
  `elapsed_ms`           BIGINT NOT NULL DEFAULT 0,
  `wpm`                  DECIMAL(8,2) DEFAULT NULL COMMENT '词/分钟',
  `accuracy`             DECIMAL(5,2) DEFAULT NULL COMMENT '按键正确率 %',
  `is_dictation`         TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默写模式',
  `dictation_score`      INT DEFAULT NULL COMMENT '默写得分（一次打对数）',
  `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE = InnoDB COMMENT = '练习记录';

-- ----------------------------
-- 单词掌握状态（错词重练，备用）
-- ----------------------------
DROP TABLE IF EXISTS `user_word_status`;
CREATE TABLE `user_word_status` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `word_id`     BIGINT DEFAULT NULL,
  `sentence_id` BIGINT DEFAULT NULL,
  `mode`        VARCHAR(10) NOT NULL,
  `wrong_count` INT NOT NULL DEFAULT 0,
  `done_count`  INT NOT NULL DEFAULT 0,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `word_id`, `sentence_id`)
) ENGINE = InnoDB COMMENT = '单词掌握状态（备用）';

-- ============================================
-- 种子数据
-- ============================================

-- 语言
INSERT INTO `language` (`id`, `name`, `code`, `sort_order`) VALUES
(1, '英语', 'en', 1),
(2, '日语', 'ja', 2);

-- 词库组（一本书）
INSERT INTO `word_group` (`id`, `language_id`, `name`, `description`, `sort_order`) VALUES
(1, 1, '小学一年级英语课本', '小学一年级英语教材，从基础单词学起', 1),
(2, 2, '标准日本语·基础会话', '标准日本语初级基础会话练习', 2);

-- 词库（一课）
INSERT INTO `word_bank` (`id`, `group_id`, `name`, `description`, `sort_order`) VALUES
(1, 1, '第一课', '认识常见事物', 1),
(2, 1, '第二课', '食物与动物', 2),
(3, 2, '第一课·基础对话', '标准日本语初级第一课对话句子', 1);

-- 单词（英语课本第一课）
INSERT INTO `word` (`word_bank_id`, `word`, `phonetic`, `meaning`, `word_type`) VALUES
(1, 'apple', '/ˈæpl/', '苹果', 'n.'),
(1, 'book', '/bʊk/', '书', 'n.'),
(1, 'cat', '/kæt/', '猫', 'n.'),
(1, 'dog', '/dɒɡ/', '狗', 'n.'),
(1, 'egg', '/eɡ/', '鸡蛋', 'n.'),
(1, 'fish', '/fɪʃ/', '鱼', 'n.'),
(1, 'girl', '/ɡɜːl/', '女孩', 'n.'),
(1, 'hat', '/hæt/', '帽子', 'n.');

-- 单词（英语课本第二课）
INSERT INTO `word` (`word_bank_id`, `word`, `phonetic`, `meaning`, `word_type`) VALUES
(2, 'ice', '/aɪs/', '冰', 'n.'),
(2, 'juice', '/dʒuːs/', '果汁', 'n.'),
(2, 'kite', '/kaɪt/', '风筝', 'n.'),
(2, 'lion', '/ˈlaɪən/', '狮子', 'n.'),
(2, 'milk', '/mɪlk/', '牛奶', 'n.'),
(2, 'nest', '/nest/', '鸟巢', 'n.'),
(2, 'orange', '/ˈɒrɪndʒ/', '橙子', 'n.'),
(2, 'pen', '/pen/', '钢笔', 'n.');

-- 单词（日语第一课）
INSERT INTO `word` (`word_bank_id`, `word`, `phonetic`, `meaning`, `word_type`) VALUES
(3, 'あう', '会う', '见面', 'v.'),
(3, 'まち', '町', '城市', 'n.'),
(3, 'でんきてん', '電気店', '电器店', 'n.'),
(3, 'ゆうめい', '有名', '有名', 'adj.'),
(3, 'おおきい', '大きい', '大的', 'adj.'),
(3, 'べんり', '便利', '方便', 'adj.'),
(3, 'しずか', '静か', '安静', 'adj.'),
(3, 'にぎやか', '賑やか', '热闹', 'adj.');

-- 句子（用户提供的 39 条数据，归入词库 3：标准日本语·第一课）
INSERT INTO `sentence` (`id`, `word_bank_id`, `chinese`, `english`, `japanese`, `sentence_type`) VALUES
(2, 3, '你需要制定一个计划', 'You need to make a plan', '計画を立てる必要があります', 'sentence'),
(3, 3, '我喜欢学习日语', 'I like learning Japanese', '日本語を勉強するのが好きです', 'sentence'),
(4, 3, '他正在吃午餐', 'He is eating lunch', '彼は昼食を食べています', 'sentence'),
(5, 3, '我们明天要去公园', 'We will go to the park tomorrow', '私たちは明日公園に行きます', 'sentence'),
(6, 3, '她每天早上跑步', 'She runs every morning', '彼女は毎朝ランニングします', 'sentence'),
(7, 3, '这个公园又宽敞又漂亮。', 'This park is spacious and clean.', 'このこうえんは　ひろくて　きれいです', 'sentence'),
(8, 3, '朋友来到了你的城市。', 'Your friend came to your city.', 'あなたの　まちに　ともだちが　きました', 'sentence'),
(9, 3, '要介绍什么样的地方呢？', 'What kind of place will you introduce?', 'どんな　ところを　しょうかいしますか', 'sentence'),
(11, 3, '秋叶原电器店很多，非常方便。', 'Akihabara has many electronics stores. It is very convenient.', 'あきはばらは　でんきてんが　おおいです。とても　べんりです。', 'sentence'),
(12, 3, '东京站很大。东京站附近有公寓。公寓很安静。', 'Tokyo Station is big. There are apartments near Tokyo Station. The apartments are quiet.', 'とうきょうえきは　おおきいです。とうきょうえきの　ちかくに　こうきょがあります。こうきょは　しずかです。', 'sentence'),
(13, 3, '筑地的鱼市场很有趣。', 'The fish market in Tsukiji is interesting.', 'つきじの　うおいちばは　おもしろいです。', 'sentence'),
(14, 3, '银座有名的店铺很多。银座的街道很时尚。', 'Ginza has many famous shops. The streets of Ginza are stylish.', 'ぎんざは　ゆうめいな　みせが　おおいです。ぎんざの　まちは　おしゃれです。', 'sentence'),
(15, 3, '原宿有可爱的店铺，年轻人很多。', 'Harajuku has cute shops. There are many young people.', 'はらじゅくには　かわいい　みせがあります。わかい　ひとが　おおいです。', 'sentence'),
(16, 3, '新宿西口高楼很多。东口居酒屋很多。夜晚非常热闹。', 'Shinjuku West Exit has many tall buildings. East Exit has many izakaya. It is very lively at night.', 'しんじゅくの　にしぐちは　たかい　ビルが　おおいです。ひがしぐちは　いざかやが　おおいです。よる　とても　にぎやかです。', 'sentence'),
(18, 3, '在百货商店的入口见面', 'Meet at the entrance of the department store', 'デパートの　入り口で　あいます', 'sentence'),
(19, 3, '在车站前面见面', 'Meet in front of the station', 'えきの　まえで　あいます', 'sentence'),
(20, 3, '在咖啡店见面', 'Meet at coffee shop', 'コーヒーショップで　あいます', 'sentence'),
(21, 3, '星期天的碰头怎么安排', 'How to arrange a meeting on Sunday', 'にちようび　まちあわせは　どうしますか', 'sentence'),
(22, 3, '1点半在富士百货的入口怎么样', 'How about the entrance of Fuji Department Store at 1:30?', '1じはんに　ふじデパートの　入り口は　どうですか', 'sentence'),
(23, 3, '1点半在富士百货的入口怎么样', 'How about the entrance of Fuji Department Store at 1:30?', '1じはんに　ふじデパートの　入り口は　どうですか', 'sentence'),
(24, 3, '1点半在富士百货的入口怎么样', 'How about the entrance of Fuji Department Store at 1:30?', '1じはんに　ふじデパートの　入り口は　どうですか', 'sentence'),
(25, 3, '4点半在咖啡店怎么样', 'How about a coffee shop at 4:30?', '4じはんに　コーヒーショップは　どうですか', 'sentence'),
(26, 3, '8点半在东京站怎么样', 'How about 8:30 at Tokyo Station?', '8じはんに　とうきょうえきは　どうですか', 'sentence'),
(27, 3, '我迟到了不好意思', 'I''m late. I''m sorry.', 'おそくなって　すみません', 'sentence'),
(28, 3, '我有点迷路了', 'I''m a little lost.', 'ちょっと　みちに　まよって', 'sentence'),
(29, 3, '有点堵车', 'A bit of traffic', 'ちょっと　じゅうたいで', 'sentence'),
(31, 3, '到目前为止，你学习过什么样的外语', 'What foreign languages have you studied so far?', 'いままでに　どんな　がいこくごを　べんきょうしましたか', 'sentence'),
(32, 3, '平假名和片假名相似', 'Hiragana and Katakana are similar', 'ひらがなは　カタカナと　にています', 'sentence'),
(34, 3, '平假名和片假名相似', 'Hiragana and Katakana are similar', 'ひらがなは　カタカナと　にています', 'sentence'),
(35, 3, '日语的文字和英语的文字不同', 'Japanese text is different from English text', 'にほんごの　もじは　英語の　もじと　ちがいます', 'sentence'),
(36, 3, '日语很简单', 'Japanese is easy', '日本語は　簡単です', 'sentence'),
(37, 3, '日语很有趣', 'Japanese is fun', '日本語は　面白いです', 'sentence'),
(38, 3, '日语很难', 'Japanese is difficult', '日本語は　難しいです', 'sentence'),
(39, 3, '日语不难', 'Japanese is not difficult', '日本語は　むずかしくないです', 'sentence'),
(40, 3, '日语学习很辛苦', 'It''s hard to learn Japanese', 'にほんごの　べんきょうは　大変です', 'sentence');

SET FOREIGN_KEY_CHECKS = 1;
