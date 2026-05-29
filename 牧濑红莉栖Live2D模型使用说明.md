# 牧濑红莉栖 Live2D 模型使用说明

本文档基于 `E:\2025llm\amadeus-system-new` 项目源码整理，说明项目中牧濑红莉栖 Live2D 模型的相关逻辑、资源来源、运行时加载方式，以及如何改成本地加载。

## 1. 结论

项目仓库本身没有内置牧濑红莉栖 Live2D 模型资源。

仓库内只有 Live2D 运行库和加载逻辑，真实模型资源来自远程静态地址：

```text
https://static.amadeus-web.top/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json
```

因此，作者说“运行项目就能有 Live2D 形象”，实际含义是：项目运行后，前端会通过网络访问作者托管的远程 Live2D 模型资源并渲染。如果远程资源不可访问，模型就无法显示。

## 2. 相关源码位置

### 2.1 Live2D 模型配置

文件：

```text
src/constants/live2d.ts
```

核心配置：

```ts
export const roleToLive2dMapper: Record<string, Live2dModel> = {
  牧濑红莉栖: {
    path: 'https://static.amadeus-web.top/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json',
    scale1: 0.42,
    scale2: 0.42,
    x1: 550,
    x2: -100,
    y1: 50,
    y2: 20
  },
}
```

这里定义了角色名到 Live2D 模型资源的映射：

- `path`：Live2D `model3.json` 的地址。
- `scale1` / `scale2`：桌面端/移动端缩放。
- `x1` / `x2`：桌面端/移动端横向位置。
- `y1` / `y2`：桌面端/移动端纵向位置。

### 2.2 Live2D 加载与渲染

文件：

```text
src/components/Live2dModel/index.tsx
```

核心逻辑：

```ts
const roleConfig = roleToLive2dMapper[role];
const model = await Live2DModel.from(roleConfig.path, {
  autoInteract: false
});
```

说明：

1. 组件根据传入的 `role` 查找 `roleToLive2dMapper`。
2. 拿到 `path` 后调用 `Live2DModel.from(...)`。
3. `pixi-live2d-display` 会读取 `model3.json`。
4. `model3.json` 再继续加载它引用的 `.moc3`、贴图、物理、表情、动作等文件。
5. 加载成功后，模型被加入 PixiJS stage：

```ts
app.stage.addChild(model);
live2dStore.setModel(model);
```

### 2.3 模型位置和缩放

同一文件中，项目根据是否移动端使用不同配置：

```ts
if (!isMobile) {
  model.scale.set(config.scale1);
  model.y = config.y1;
  model.x = config.x1 + (window.innerWidth - 1620) / 2;
} else {
  model.scale.set(config.scale2);
  model.x = config.x2;
  model.y = config.y2;
}
```

桌面端会根据窗口宽度额外修正 `x` 坐标。

### 2.4 Live2D 全局状态

文件：

```text
src/store/live2d/index.ts
```

核心状态：

```ts
model: Live2DModel | null = null;
emotion: string | null = null;
motion: string | null = null;
```

用途：

- `model`：保存当前 Live2D 实例。
- `emotion`：当前表情名。
- `motion`：当前动作名。

`Live2dModel` 组件监听这些状态变化，然后对模型调用表情或动作 API。

## 3. 表情与动作逻辑

### 3.1 表情控制

文件：

```text
src/components/Live2dModel/index.tsx
```

核心逻辑：

```ts
const handleEmotion = (emotion: string) => {
  if (emotion === 'neutral') {
    modelRef.current?.internalModel.motionManager.expressionManager.resetExpression();
    return;
  }

  if (!modelRef.current?.internalModel?.motionManager?.expressionManager) return;
  modelRef.current.internalModel.motionManager.expressionManager.setExpression(emotion);
};
```

说明：

- `neutral` 会重置表情。
- 非 `neutral` 会调用 `setExpression(emotion)`。
- `emotion` 字符串必须和模型资源里的 `.exp3.json` 表情名称匹配。

项目里情绪分析返回的可选值来自：

```text
service/webrtc/ai/emotion.py
```

枚举包括：

```text
neutral, anger, joy, sadness, shy, shy2, smile1, smile2, unhappy
```

### 3.2 动作控制

核心逻辑：

```ts
const handleMotion = (motion: string) => {
  if (!modelRef.current?.internalModel?.motionManager) return;
  modelRef.current.internalModel.motionManager.startMotion(motion, 0, MotionPriority.FORCE);
};
```

随机动作：

```ts
const RANDOM_MOTIONS = ['random1', 'random2', 'random3', 'random4', 'random5'];
```

当状态为 `speaking` 或 `thinking` 时，项目会播放随机动作：

```ts
if (live2dStore.motion === 'speaking' || live2dStore.motion === 'thinking') {
  playRandomMotion();
} else {
  handleMotion(live2dStore.motion);
}
```

### 3.3 情绪如何传到 Live2D

文件：

```text
src/pages/Home/index.tsx
```

核心逻辑：

```ts
onEmotionResponse: (emotion: string) => {
  live2dStore.setEmotion(emotion || '');
  live2dStore.setMotion(emotion || '');
}
```

后端生成回复后会调用情绪分析，前端收到 `emotion_response` 后同时设置：

- `live2dStore.emotion`
- `live2dStore.motion`

因此，后端情绪名最好同时存在对应的 expression 和 motion，否则可能只有表情或动作生效。

## 4. 牧濑红莉栖模型资源组成

远程 `红莉栖.model3.json` 引用的资源大致包括：

```text
红莉栖.model3.json
红莉栖.moc3
红莉栖.physics3.json
红莉栖.cdi3.json
红莉栖.2048/texture_00.png
```

表情文件：

```text
anger.exp3.json
joy.exp3.json
neutral.exp3.json
sadness.exp3.json
shy.exp3.json
shy2.exp3.json
smile1.exp3.json
smile2.exp3.json
surprise.exp3.json
unhappy.exp3.json
```

动作文件：

```text
neutral.motion3.json
anger.motion3.json
joy.motion3.json
sadness.motion3.json
shy.motion3.json
shy2.motion3.json
smile1.motion3.json
smile2.motion3.json
surprise.motion3.json
unhappy.motion3.json
random1.motion3.json
random2.motion3.json
random3.motion3.json
random4.motion3.json
random5.motion3.json
```

其中贴图文件是：

```text
红莉栖.2048/texture_00.png
```

只下载 `model3.json` 不够，必须把它引用的所有文件按相对路径保存下来。

## 5. 下载到本地的 PowerShell 脚本

建议下载到项目的 `public` 目录：

```text
E:\2025llm\amadeus-system-new\public\live2dmodels\steinsGateKurisuNew
```

PowerShell：

```powershell
$base = "https://static.amadeus-web.top/live2dmodels/steinsGateKurisuNew"
$out = "E:\2025llm\amadeus-system-new\public\live2dmodels\steinsGateKurisuNew"

New-Item -ItemType Directory -Force $out | Out-Null
New-Item -ItemType Directory -Force "$out\红莉栖.2048" | Out-Null

$files = @(
  "红莉栖.model3.json",
  "红莉栖.moc3",
  "红莉栖.physics3.json",
  "红莉栖.cdi3.json",
  "红莉栖.2048/texture_00.png",
  "anger.exp3.json", "joy.exp3.json", "neutral.exp3.json", "sadness.exp3.json",
  "shy.exp3.json", "shy2.exp3.json", "smile1.exp3.json", "smile2.exp3.json",
  "surprise.exp3.json", "unhappy.exp3.json",
  "neutral.motion3.json", "anger.motion3.json", "joy.motion3.json",
  "sadness.motion3.json", "shy.motion3.json", "shy2.motion3.json",
  "smile1.motion3.json", "smile2.motion3.json", "surprise.motion3.json",
  "unhappy.motion3.json", "random1.motion3.json", "random2.motion3.json",
  "random3.motion3.json", "random4.motion3.json", "random5.motion3.json"
)

foreach ($file in $files) {
  $url = "$base/$([uri]::EscapeUriString($file))"
  $dest = Join-Path $out $file
  $parent = Split-Path $dest -Parent
  New-Item -ItemType Directory -Force $parent | Out-Null

  Write-Host "Downloading $file"
  Invoke-WebRequest -Uri $url -OutFile $dest -ErrorAction Stop
}
```

## 6. 改成本地加载

下载完成后，修改：

```text
src/constants/live2d.ts
```

把远程地址：

```ts
path: 'https://static.amadeus-web.top/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json'
```

改成本地 public 路径：

```ts
path: '/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json'
```

Vite 会把 `public` 目录作为站点根目录暴露，所以浏览器访问路径是：

```text
/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json
```

不是：

```text
/public/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json
```

## 7. 项目运行时的 Live2D 调用链

整体链路：

```text
src/constants/live2d.ts
  -> 定义角色名和 model3.json 地址

src/components/Live2dModel/index.tsx
  -> 读取 roleToLive2dMapper[role]
  -> 调 Live2DModel.from(path)
  -> 加载 model3.json 及其引用资源
  -> app.stage.addChild(model)
  -> live2dStore.setModel(model)

src/store/live2d/index.ts
  -> 保存 model / emotion / motion

service/webrtc/ai/emotion.py
  -> 后端根据 AI 回复判断情绪

src/hooks/useWebRTC.ts
  -> 收到 SSE 的 emotion_response

src/pages/Home/index.tsx
  -> live2dStore.setEmotion(emotion)
  -> live2dStore.setMotion(emotion)

src/components/Live2dModel/index.tsx
  -> setExpression(emotion)
  -> startMotion(motion, 0, MotionPriority.FORCE)
```

## 8. 常见问题

### 8.1 为什么只下载贴图还不能显示？

因为贴图只是模型外观图片。Live2D 还需要：

- `model3.json`：入口配置。
- `.moc3`：模型核心数据。
- texture：贴图。
- physics：物理配置。
- expression：表情。
- motion：动作。

缺少 `.moc3` 或贴图通常会导致模型完全无法显示。

### 8.2 为什么本地路径不能写 `public/...`？

Vite 的 `public` 目录会被映射为站点根目录。

文件实际位置：

```text
public/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json
```

代码里应该写：

```text
/live2dmodels/steinsGateKurisuNew/红莉栖.model3.json
```

### 8.3 表情不生效怎么办？

检查后端返回的 emotion 名称是否和模型中的表情文件名一致。例如：

```text
anger.exp3.json -> anger
joy.exp3.json -> joy
sadness.exp3.json -> sadness
```

前端调用的是：

```ts
setExpression(emotion)
```

所以名称不匹配时，表情不会正常切换。

### 8.4 动作不生效怎么办？

检查 motion 名称是否存在对应动作文件。例如：

```text
random1.motion3.json -> random1
smile1.motion3.json -> smile1
```

前端调用的是：

```ts
startMotion(motion, 0, MotionPriority.FORCE)
```

名称不匹配时，动作不会正常播放。

## 9. 授权提醒

这些 Live2D 模型资源来自远程静态站点，技术上可以下载用于本地加载，但模型、贴图、动作、表情可能涉及原作者或版权方授权。建议仅在有权限的范围内用于本地研究、调试或个人学习。
