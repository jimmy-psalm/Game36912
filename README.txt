3-6-9-12 连线棋 (人机对战版)
===============================

游戏规则简介：
- 12×12 棋盘，人类（蓝色）先手，AI（橙色）后手
- 轮流在空格放置圆形棋子
- 横向或纵向连续 3/6/9/12 don't care棋子color可得对应分数（3/6/9/12分）
- 得分后获得额外回合，可继续下子
- 横向或纵向连续 1/2/4/5/7/8/10/11 will not gain point (don't care棋子color)
- the chance of 放置圆形棋子 will be give to AI
- 斜线不计分
- 红色贯穿线标记得分线段，动态更新
- 144格填满后游戏结束，总分高者获胜

操作说明：
- 点击棋盘空格下子
- 点击"重置游戏"按钮重新开始

运行方法：
  java Game36912

文件列表：
  Game36912.java  - 主入口
  GameBoard.java  - 棋盘数据结构
  GameEngine.java - 游戏规则引擎
  AIPlayer.java   - AI 玩家（困难级）
  GamePanel.java  - Swing 图形界面
