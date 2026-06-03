<template>
  <main class="kami-page">
    <header class="nav-pill">
      <NuxtLink to="/" class="brand">
        <span class="crest">◇</span>
        卡米API
      </NuxtLink>
      <nav class="links" aria-label="Primary navigation">
        <NuxtLink to="/" class="link">主页</NuxtLink>
        <a class="link" href="#">控制台</a>
        <a class="link" href="#models">模型广场</a>
        <NuxtLink to="/" class="link active">定价</NuxtLink>
        <a class="link" href="#ranking">排行榜</a>
        <a class="link" href="#docs">文档</a>
        <a class="link" href="#about">关于</a>
      </nav>
      <div class="nav-actions">
        <button>文A</button>
        <button>☼</button>
        <button class="bell">♧<span>7</span></button>
        <a href="#" class="login">登录</a>
      </div>
    </header>

    <section class="hero section-paper">
      <div class="rings rings-a" />
      <div class="rings rings-b" />
      <div class="rings rings-c" />
      <div class="hero-copy">
        <h1>顶级模型<br>即刻接入</h1>
        <p class="mono">PREMIUM MODELS · ONE ACCESS</p>
        <div class="chips">
          <span>Image 2 · 1 毛 / 张</span>
          <span>Grok · 5 分 / 张起</span>
          <span>特惠 Codex 周卡 4 / 天</span>
          <span>特价 Claude 同步接入</span>
        </div>
      </div>
    </section>

    <section id="models" class="model-brief section-paper">
      <div class="burst burst-main" />
      <div class="burst burst-price" />
      <div class="float-squares" />
      <div class="brief-left">
        <p class="mono">GPT SERIES · FLAGSHIP POOL</p>
        <h2>GPT<br>旗舰模型</h2>
        <p class="lead">支持 5.5 / 5.4，官网同步接入。</p>
        <div class="mini-tags">
          <span>官网同价</span>
          <span>缓存命中率 90%+</span>
          <span>不掺水</span>
          <span>价格真实</span>
        </div>
      </div>
      <div class="brief-right">
        <div>
          <h3>Pro+Plus</h3>
          <p>旗舰号池</p>
        </div>
        <div>
          <strong>0.16</strong>
          <span>元 / 万</span>
        </div>
        <div>
          <h3>Free+Plus</h3>
          <p>特惠分组</p>
        </div>
        <div>
          <strong>0.04</strong>
          <span>元 / 万</span>
        </div>
      </div>
    </section>

    <section class="price-paper">
      <div class="pin pin-left" />
      <div class="pin pin-right" />
      <p class="mono">06 · TRANSPARENCY · WANTED FILE</p>
      <h2>别被「1元=100万」骗，透明<br>真正便宜看 <em>实际消耗。</em></h2>
      <p class="paper-desc">不做虚高面额，不做套路分组。Plus+Pro 号池 0.16x 起步，周卡余额四分特惠，缓存命中率按真实消耗计算。</p>
      <div class="feature-row">
        <article>
          <span>01</span>
          <h3>透明额度</h3>
          <p>账面、倍率、消耗关系都写清楚。</p>
        </article>
        <article>
          <span>02</span>
          <h3>Plus+Pro</h3>
          <p>旗舰号池 0.16x 起步，不缩水模型。</p>
        </article>
        <article>
          <span>03</span>
          <h3>缓存不掺水</h3>
          <p>缓存命中率 90%+，按真实命中结算。</p>
        </article>
        <article>
          <span>04</span>
          <h3>周卡余额</h3>
          <p>四分特惠，账单变化可追踪。</p>
        </article>
      </div>
      <h3 class="saving">价格<em>透明</em>，才是真的省钱</h3>
      <div class="compare">
        <div>
          <p class="mono">同类常见做法</p>
          <p>× 用夸张额度制造便宜感，实际成本不低。</p>
          <p>× 缓存、上下文、工具调用分散计费。</p>
          <p>× 低价分组混入缩水能力，体验不可预期。</p>
        </div>
        <div>
          <p class="mono">KAMIAPI 的做法</p>
          <p>✓ Plus+Pro 0.16x 起步，倍率明示。</p>
          <p>✓ 缓存命中不掺水，真实省下的才算优惠。</p>
          <p>✓ 周卡余额四分特惠，适合高频试用。</p>
        </div>
      </div>
    </section>

    <section id="ranking" class="forest section-paper">
      <div class="forest-lines" />
      <div class="forest-copy">
        <p class="mono">MODEL BRIEF</p>
        <h2>模型森林</h2>
        <p>主流强力大模型优选渠道，随时接入，欢迎随时体验。</p>
      </div>
      <div class="model-cloud">
        <span class="m1">GPT 5.5<small>FLAGSHIP</small></span>
        <span class="m2">GPT 5.4<small>STABLE</small></span>
        <span class="m3">Claude<small>SYNC</small></span>
        <span class="m4">Grok<small>IMAGINE</small></span>
        <span class="m5">Image 2<small>AGENTIC</small></span>
        <span class="m6">Gemini<small>VISION</small></span>
        <span class="m7">Codex<small>WEEKLY</small></span>
      </div>
    </section>

    <footer class="footer">
      <span>MODEL PLAZA · ACT I · KMSOFT STUDIO © 2026</span>
      <span>Actual pricing is subject to recharge rate & group ratio.</span>
    </footer>
  </main>
</template>

<style scoped>
.kami-page {
  min-height: 100vh;
  color: #050505;
  background: #f4f0e8;
  font-family: Georgia, "Times New Roman", "Noto Serif SC", SimSun, serif;
}

.nav-pill {
  position: fixed;
  top: 14px;
  left: 50%;
  z-index: 50;
  width: min(810px, calc(100% - 32px));
  height: 48px;
  display: flex;
  align-items: center;
  gap: 26px;
  padding: 0 10px 0 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(18px);
  transform: translateX(-50%);
}

.brand,
.link,
.login {
  color: inherit;
  text-decoration: none;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  font-family: ui-sans-serif, system-ui, sans-serif;
  font-size: 15px;
  font-weight: 900;
}

.crest {
  font-family: Georgia, serif;
  font-size: 22px;
  line-height: 1;
}

.links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 22px;
  flex: 1;
  font-family: ui-sans-serif, system-ui, sans-serif;
  color: #555;
  font-size: 13px;
  font-weight: 700;
}

.link.active,
.link:hover {
  color: #000;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.nav-actions button {
  position: relative;
  border: 0;
  padding: 0;
  color: #050505;
  background: transparent;
  font-size: 16px;
  font-weight: 800;
}

.bell span {
  position: absolute;
  top: -10px;
  right: -8px;
  min-width: 18px;
  height: 18px;
  display: grid;
  place-items: center;
  color: #d92323;
  border-radius: 999px;
  background: #ffdede;
  font: 11px/1 ui-sans-serif, system-ui, sans-serif;
}

.login {
  padding: 9px 18px;
  color: #fff;
  border-radius: 999px;
  background: #050505;
  font: 800 13px/1 ui-sans-serif, system-ui, sans-serif;
}

.section-paper {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  background: #f4f0e8;
}

.hero {
  display: grid;
  place-items: center;
  text-align: center;
}

.hero::after,
.model-brief::after,
.forest::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(0, 0, 0, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.035) 1px, transparent 1px);
  background-size: 72px 72px;
}

.hero-copy {
  position: relative;
  z-index: 2;
  margin-top: 42px;
}

.hero h1 {
  margin: 0;
  font-size: clamp(82px, 8vw, 148px);
  font-weight: 500;
  line-height: 0.95;
  letter-spacing: 0.02em;
}

.mono {
  font-family: "Courier New", monospace;
  letter-spacing: 0.42em;
  color: rgba(0, 0, 0, 0.58);
  font-size: 12px;
  text-transform: uppercase;
}

.chips {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 32px;
  font-family: "Courier New", monospace;
  color: rgba(0, 0, 0, 0.72);
  font-size: 12px;
}

.chips span,
.mini-tags span {
  min-width: 180px;
  padding: 13px 18px;
  border: 1px solid rgba(0, 0, 0, 0.18);
  background: rgba(244, 240, 232, 0.62);
}

.rings {
  position: absolute;
  border: 1px solid rgba(0, 0, 0, 0.14);
  border-radius: 50%;
  transform: rotate(-2deg);
}

.rings::before,
.rings::after {
  content: '';
  position: absolute;
  inset: 12px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: inherit;
}

.rings::after {
  inset: 25px;
}

.rings-a {
  width: 880px;
  height: 126px;
  left: 26%;
  top: 42%;
}

.rings-b {
  width: 520px;
  height: 92px;
  right: 7%;
  top: 38%;
  opacity: 0.45;
}

.rings-c {
  width: 660px;
  height: 114px;
  right: 18%;
  top: 55%;
  opacity: 0.28;
}

.model-brief {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  padding: 120px 12vw 80px;
}

.brief-left,
.brief-right {
  position: relative;
  z-index: 2;
}

.brief-left h2 {
  margin: 30px 0 20px;
  font-size: clamp(72px, 6vw, 116px);
  font-weight: 500;
  line-height: 0.86;
}

.lead {
  color: rgba(0, 0, 0, 0.48);
  font-size: 20px;
}

.mini-tags {
  display: flex;
  gap: 8px;
  margin-top: 34px;
  font-family: "Courier New", monospace;
  font-size: 12px;
}

.mini-tags span {
  min-width: auto;
  padding: 10px 14px;
}

.brief-right {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px 110px;
  align-items: center;
}

.brief-right h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 500;
}

.brief-right p,
.brief-right span {
  color: rgba(0, 0, 0, 0.4);
}

.brief-right strong {
  display: block;
  font-size: 54px;
  font-weight: 500;
}

.burst {
  position: absolute;
  z-index: 1;
  width: 420px;
  height: 420px;
  background: repeating-conic-gradient(from 12deg, rgba(0,0,0,.46) 0 1deg, transparent 1deg 8deg);
  clip-path: circle(50%);
  opacity: 0.55;
  mask-image: radial-gradient(circle, transparent 0 18px, #000 19px, transparent 52%);
}

.burst-main {
  left: 43%;
  top: 25%;
}

.burst-price {
  right: 10%;
  top: 47%;
  width: 220px;
  height: 220px;
}

.float-squares {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background:
    linear-gradient(25deg, transparent 30%, rgba(0,0,0,.16) 31% 36%, transparent 37%) 0 0 / 210px 180px,
    linear-gradient(-20deg, transparent 42%, rgba(0,0,0,.14) 43% 50%, transparent 51%) 30px 20px / 260px 220px;
}

.price-paper {
  position: relative;
  width: min(1180px, calc(100% - 80px));
  margin: 80px auto;
  padding: 48px 58px 54px;
  border: 1px solid rgba(0, 0, 0, 0.18);
  outline: 12px solid rgba(0, 0, 0, 0.035);
  background:
    radial-gradient(circle, rgba(0,0,0,.055) 1px, transparent 1px) 0 0 / 7px 7px,
    #e8e4dc;
  box-shadow: 0 35px 100px rgba(0, 0, 0, 0.28);
}

.price-paper h2 {
  margin: 12px 0 16px;
  max-width: 980px;
  font-size: clamp(58px, 5vw, 92px);
  font-weight: 700;
  line-height: 0.98;
  letter-spacing: -0.04em;
}

.price-paper em,
.saving em {
  color: #a8322b;
  font-style: normal;
}

.paper-desc {
  max-width: 920px;
  color: rgba(0, 0, 0, 0.52);
  font-size: 18px;
  line-height: 1.8;
}

.feature-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  margin-top: 34px;
  border-top: 1px solid rgba(0,0,0,.18);
  border-bottom: 1px solid rgba(0,0,0,.18);
}

.feature-row article {
  min-height: 112px;
  padding: 24px;
  border-right: 1px solid rgba(0,0,0,.14);
}

.feature-row article:last-child {
  border-right: 0;
}

.feature-row span {
  font-family: "Courier New", monospace;
  color: rgba(0,0,0,.35);
  font-size: 11px;
}

.feature-row h3 {
  margin: 12px 0 8px;
  font-size: 22px;
}

.feature-row p,
.compare p {
  color: rgba(0,0,0,.5);
  line-height: 1.7;
}

.saving {
  margin: 42px 0 24px;
  text-align: center;
  font-size: clamp(44px, 4vw, 70px);
  line-height: 1;
}

.compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border: 1px solid rgba(0,0,0,.16);
}

.compare > div {
  padding: 24px 30px;
}

.compare > div + div {
  border-left: 1px solid rgba(0,0,0,.16);
}

.pin {
  position: absolute;
  top: 22px;
  width: 16px;
  height: 16px;
  border-radius: 999px;
  background: radial-gradient(circle at 35% 35%, #d36b62, #7e1712);
  box-shadow: 0 5px 8px rgba(0,0,0,.35);
}

.pin-left { left: 28px; }
.pin-right { right: 28px; }

.forest {
  display: grid;
  grid-template-columns: 44% 56%;
  align-items: center;
  padding: 120px 12vw 80px;
}

.forest-copy,
.model-cloud {
  position: relative;
  z-index: 2;
}

.forest-copy h2 {
  margin: 20px 0;
  font-size: clamp(72px, 6vw, 108px);
  font-weight: 500;
  line-height: 1;
}

.forest-copy p:not(.mono) {
  max-width: 520px;
  color: rgba(0,0,0,.45);
  font-size: 20px;
}

.forest-lines {
  position: absolute;
  inset: 0;
  opacity: .36;
  background: repeating-linear-gradient(82deg, transparent 0 70px, rgba(0,0,0,.28) 71px 72px, transparent 73px 130px);
  mask-image: linear-gradient(to top, #000 0 58%, transparent 92%);
}

.model-cloud {
  min-height: 520px;
}

.model-cloud span {
  position: absolute;
  color: rgba(0,0,0,.55);
  font-size: 48px;
  line-height: .9;
}

.model-cloud small {
  display: block;
  margin-top: 14px;
  font-family: "Courier New", monospace;
  font-size: 9px;
  letter-spacing: .42em;
}

.m1 { left: 8%; top: 8%; }
.m2 { right: 12%; top: 5%; }
.m3 { left: 28%; top: 28%; }
.m4 { right: 8%; top: 38%; }
.m5 { left: 9%; top: 46%; }
.m6 { right: 22%; top: 56%; }
.m7 { left: 34%; top: 72%; }

.footer {
  min-height: 56px;
  display: flex;
  align-items: center;
  gap: 34px;
  padding: 0 9vw;
  color: #b9b9b9;
  background: #050505;
  font-family: "Courier New", monospace;
  letter-spacing: .32em;
  font-size: 12px;
}

@media (max-width: 1000px) {
  .links { display: none; }
  .nav-pill { justify-content: space-between; }
  .hero h1 { font-size: 72px; }
  .chips, .mini-tags { flex-wrap: wrap; }
  .model-brief,
  .forest {
    grid-template-columns: 1fr;
    padding: 110px 28px 70px;
  }
  .brief-right { margin-top: 80px; }
  .feature-row,
  .compare { grid-template-columns: 1fr; }
  .feature-row article,
  .compare > div + div { border-left: 0; border-right: 0; border-top: 1px solid rgba(0,0,0,.16); }
}

@media (max-width: 640px) {
  .nav-actions button { display: none; }
  .hero h1 { font-size: 54px; }
  .chips span { min-width: 100%; }
  .price-paper { width: calc(100% - 32px); padding: 34px 22px; }
  .price-paper h2 { font-size: 42px; }
  .footer { flex-direction: column; align-items: flex-start; justify-content: center; }
}
</style>
