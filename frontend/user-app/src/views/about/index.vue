<script setup lang="ts">
</script>

<template>
  <div class="about-page">
    <section class="about-hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">关于我们</h1>
        <p class="hero-subtitle">传承新疆特色文化，让世界品味新疆</p>
      </div>
    </section>

    <section class="about-intro">
      <div class="container">
        <div class="intro-content">
          <div class="intro-text">
            <h2 class="section-title">新疆文旅商城</h2>
            <p class="intro-paragraph">
              新疆文旅商城是一个专注于新疆特色产品销售的电商平台。我们致力于将新疆最优质的特产、最精美的文创产品、最纯正的美食带给全国乃至全世界的消费者。
            </p>
            <p class="intro-paragraph">
              在这里，您可以找到新疆特色干果、新鲜水果、醇香美酒、传统手工艺品等各种优质商品。每一件商品都经过我们严格筛选，确保品质上乘。
            </p>
          </div>
          <div class="intro-image">
            <img src="https://pic.netbian.com/uploads/allimg/170512/172852-1494581332b5a0.jpg" alt="新疆风光" />
          </div>
        </div>
      </div>
    </section>

    <section class="about-mission">
      <div class="container">
        <h2 class="section-title">我们的使命</h2>
        <div class="mission-grid">
          <div class="mission-item">
            <div class="mission-icon">🌿</div>
            <h3>品质保证</h3>
            <p>严格筛选每一件商品，确保从产地直达您的餐桌</p>
          </div>
          <div class="mission-item">
            <div class="mission-icon">🎁</div>
            <h3>特色产品</h3>
            <p>汇聚新疆各地特色好物，让您一站式购齐</p>
          </div>
          <div class="mission-item">
            <div class="mission-icon">💝</div>
            <h3>用心服务</h3>
            <p>以客户为中心，提供最贴心的购物体验</p>
          </div>
          <div class="mission-item">
            <div class="mission-icon">✈️</div>
            <h3>快速配送</h3>
            <p>专业物流团队，确保商品新鲜送达</p>
          </div>
        </div>
      </div>
    </section>

    <section class="about-features">
      <div class="container">
        <h2 class="section-title">为什么选择我们</h2>
        <div class="features-content">
          <div class="feature-block">
            <h3>产地直采</h3>
            <p>我们与新疆各地农户、合作社直接合作，从源头把控品质，确保您收到的每一件商品都是最纯正的新疆味道。</p>
          </div>
          <div class="feature-block">
            <h3>严格品控</h3>
            <p>每一批商品都经过多重检验，从采购到发货全程质量监控，让您买得放心、吃得安心。</p>
          </div>
          <div class="feature-block">
            <h3>文化传承</h3>
            <p>我们不仅销售商品，更是新疆文化的传播者。通过我们的平台，让更多人了解新疆、爱上新疆。</p>
          </div>
        </div>
      </div>
    </section>

    <section class="about-contact">
      <div class="container">
        <h2 class="section-title">联系我们</h2>
        <div class="contact-info">
          <div class="contact-item">
            <div class="contact-icon">📍</div>
            <div class="contact-text">
              <h4>地址</h4>
              <p>新疆维吾尔自治区乌鲁木齐市</p>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">📞</div>
            <div class="contact-text">
              <h4>电话</h4>
              <p>400-123-4567</p>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">📧</div>
            <div class="contact-text">
              <h4>邮箱</h4>
              <p>info@xinjiangmall.com</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-page {
  background: #F8F9FA;
}

.about-hero {
  position: relative;
  height: 400px;
  background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)),
    url('https://c.53326.com/d/file/lan20210602/5xjbzj2offf.jpg') center/cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.hero-content {
  text-align: center;
  color: white;
  z-index: 1;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.95;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.about-intro {
  padding: 80px 0;
  background: #fff;
}

.intro-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.intro-text h2 {
  font-size: 32px;
  color: #2C3E50;
  margin-bottom: 24px;
}

.intro-paragraph {
  font-size: 16px;
  line-height: 1.8;
  color: #555;
  margin-bottom: 16px;
}

.intro-image img {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.about-mission {
  padding: 80px 0;
  background: #F8F9FA;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: bold;
  color: #2C3E50;
  margin-bottom: 48px;
}

.mission-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.mission-item {
  background: #fff;
  padding: 40px 24px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.mission-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.mission-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.mission-item h3 {
  font-size: 20px;
  color: #2C3E50;
  margin-bottom: 12px;
}

.mission-item p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.about-features {
  padding: 80px 0;
  background: #fff;
}

.features-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.feature-block {
  text-align: center;
}

.feature-block h3 {
  font-size: 22px;
  color: #E67E22;
  margin-bottom: 16px;
}

.feature-block p {
  font-size: 15px;
  color: #555;
  line-height: 1.8;
  margin: 0;
}

.about-contact {
  padding: 80px 0;
  background: #2C3E50;
}

.about-contact .section-title {
  color: #fff;
}

.contact-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  max-width: 900px;
  margin: 0 auto;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
}

.contact-icon {
  font-size: 40px;
  flex-shrink: 0;
}

.contact-text h4 {
  font-size: 18px;
  margin-bottom: 8px;
}

.contact-text p {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
}

@media (max-width: 992px) {
  .about-hero {
    height: 300px;
  }

  .hero-title {
    font-size: 36px;
  }

  .intro-content {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .mission-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .features-content {
    grid-template-columns: 1fr;
  }

  .contact-info {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .about-hero {
    height: 250px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .about-intro,
  .about-mission,
  .about-features,
  .about-contact {
    padding: 50px 0;
  }

  .section-title {
    font-size: 24px;
    margin-bottom: 32px;
  }

  .intro-text h2 {
    font-size: 24px;
  }

  .mission-grid {
    grid-template-columns: 1fr;
  }

  .mission-item {
    padding: 32px 20px;
  }
}
</style>
