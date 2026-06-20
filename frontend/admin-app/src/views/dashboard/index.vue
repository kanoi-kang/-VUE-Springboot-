<script setup lang="ts">import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { getAdminDashboard, getRevenueTrend, getOrderTrend, getTopProducts, getCategoryStats } from '@/api/admin';
const stats = ref({
 todayRevenue: 0,
 todayOrders: 0,
 totalOrders: 0,
 totalProducts: 0,
 totalUsers: 0,
 totalRevenue: 0,
 pendingOrders: 0,
 completedOrders: 0
});
const loading = ref(true);
const revenueChartRef = ref<HTMLElement | null>(null);
const orderChartRef = ref<HTMLElement | null>(null);
const productChartRef = ref<HTMLElement | null>(null);
const categoryChartRef = ref<HTMLElement | null>(null);
let revenueChartInstance: echarts.ECharts | null = null;
let orderChartInstance: echarts.ECharts | null = null;
let productChartInstance: echarts.ECharts | null = null;
let categoryChartInstance: echarts.ECharts | null = null;
const initCharts = () => {
 if (revenueChartRef.value) {
 revenueChartInstance = echarts.init(revenueChartRef.value);
 }
 if (orderChartRef.value) {
 orderChartInstance = echarts.init(orderChartRef.value);
 }
 if (productChartRef.value) {
 productChartInstance = echarts.init(productChartRef.value);
 }
 if (categoryChartRef.value) {
 categoryChartInstance = echarts.init(categoryChartRef.value);
 }
};
const loadRevenueChart = async () => {
 try {
 const res: any = await getRevenueTrend(7);
 const data = res?.data?.revenue || [];
 const dates = data.map((item: any) => item.date || '');
 const revenues = data.map((item: any) => item.revenue || 0);
 if (revenueChartInstance) {
 revenueChartInstance.setOption({
 tooltip: {
 trigger: 'axis',
 formatter: '{b}<br/>销售额: ¥{c}'
 },
 grid: {
 left: '3%',
 right: '4%',
 bottom: '3%',
 containLabel: true
 },
 xAxis: {
 type: 'category',
 data: dates,
 axisLine: {
 lineStyle: { color: '#ddd' }
 },
 axisLabel: {
 color: '#666',
 fontSize: 12
 }
 },
 yAxis: {
 type: 'value',
 axisLine: { show: false },
 axisTick: { show: false },
 splitLine: {
 lineStyle: { color: '#f0f0f0' }
 },
 axisLabel: {
 color: '#666',
 fontSize: 12,
 formatter: '¥{value}'
 }
 },
 series: [{
 data: revenues,
 type: 'line',
 smooth: true,
 symbol: 'circle',
 symbolSize: 8,
 lineStyle: {
 width: 3,
 color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
 { offset: 0, color: '#667eea' },
 { offset: 1, color: '#764ba2' }
 ])
 },
 itemStyle: {
 color: '#667eea'
 },
 areaStyle: {
 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
 { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
 { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
 ])
 }
 }]
 });
 }
 }
 catch (error) {
 console.error('Failed to load revenue chart:', error);
 }
};
const loadOrderChart = async () => {
 try {
 const res: any = await getOrderTrend(7);
 const data = res?.data?.orders || [];
 const dates = data.map((item: any) => item.date || '');
 const orders = data.map((item: any) => item.orders || 0);
 if (orderChartInstance) {
 orderChartInstance.setOption({
 tooltip: {
 trigger: 'axis',
 formatter: '{b}<br/>订单数: {c}单'
 },
 grid: {
 left: '3%',
 right: '4%',
 bottom: '3%',
 containLabel: true
 },
 xAxis: {
 type: 'category',
 data: dates,
 axisLine: {
 lineStyle: { color: '#ddd' }
 },
 axisLabel: {
 color: '#666',
 fontSize: 12
 }
 },
 yAxis: {
 type: 'value',
 axisLine: { show: false },
 axisTick: { show: false },
 splitLine: {
 lineStyle: { color: '#f0f0f0' }
 },
 axisLabel: {
 color: '#666',
 fontSize: 12
 }
 },
 series: [{
 data: orders,
 type: 'line',
 smooth: true,
 symbol: 'circle',
 symbolSize: 8,
 lineStyle: {
 width: 3,
 color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
 { offset: 0, color: '#11998e' },
 { offset: 1, color: '#38ef7d' }
 ])
 },
 itemStyle: {
 color: '#11998e'
 },
 areaStyle: {
 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
 { offset: 0, color: 'rgba(17, 153, 142, 0.3)' },
 { offset: 1, color: 'rgba(17, 153, 142, 0.05)' }
 ])
 }
 }]
 });
 }
 }
 catch (error) {
 console.error('Failed to load order chart:', error);
 }
};
const loadProductChart = async () => {
 try {
 console.log('Loading product chart...');
 const res: any = await getTopProducts(5);
 console.log('Product chart API response:', res);
 const data = res?.data?.products || [];
 console.log('Product data:', data);
 const names = data.map((item: any) => item.productName || '');
 const sales = data.map((item: any) => item.salesCount || 0);
 console.log('Product names:', names);
 console.log('Product sales:', sales);
 console.log('productChartInstance:', productChartInstance);
 if (productChartInstance) {
 productChartInstance.setOption({
 tooltip: {
 trigger: 'axis',
 axisPointer: { type: 'shadow' },
 formatter: '{b}<br/>销量: {c}件'
 },
 grid: {
 left: '3%',
 right: '4%',
 bottom: '3%',
 containLabel: true
 },
 xAxis: {
 type: 'category',
 data: names,
 axisLine: {
 lineStyle: { color: '#ddd' }
 },
 axisLabel: {
 color: '#666',
 fontSize: 12,
 interval: 0,
 rotate: 15
 }
 },
 yAxis: {
 type: 'value',
 axisLine: { show: false },
 axisTick: { show: false },
 splitLine: {
 lineStyle: { color: '#f0f0f0' }
 },
 axisLabel: {
 color: '#666',
 fontSize: 12
 }
 },
 series: [{
 data: sales,
 type: 'bar',
 barWidth: '50%',
 itemStyle: {
 borderRadius: [6, 6, 0, 0],
 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
 { offset: 0, color: '#f093fb' },
 { offset: 1, color: '#f5576c' }
 ])
 }
 }]
 });
 }
 }
 catch (error) {
 console.error('Failed to load product chart:', error);
 }
};
const loadCategoryChart = async () => {
 try {
 console.log('Loading category chart...');
 const res: any = await getCategoryStats();
 console.log('Category chart API response:', res);
 const data = res?.data?.categories || [];
 console.log('Category data:', data);
 const pieData = data.map((item: any) => ({
 name: item.categoryName || '',
 value: item.productCount || 0
 }));
 console.log('Pie data:', pieData);
 console.log('categoryChartInstance:', categoryChartInstance);
 if (categoryChartInstance) {
 categoryChartInstance.setOption({
 tooltip: {
 trigger: 'item',
 formatter: '{b}: {c}件 ({d}%)'
 },
 legend: {
 orient: 'horizontal',
 bottom: '5%',
 textStyle: {
 color: '#666',
 fontSize: 12
 }
 },
 series: [{
 type: 'pie',
 radius: ['40%', '70%'],
 center: ['50%', '45%'],
 avoidLabelOverlap: false,
 itemStyle: {
 borderRadius: 8,
 borderColor: '#fff',
 borderWidth: 2
 },
 label: {
 show: false,
 position: 'center'
 },
 emphasis: {
 label: {
 show: true,
 fontSize: 18,
 fontWeight: 'bold',
 formatter: '{b}\n{d}%'
 }
 },
 labelLine: {
 show: false
 },
 data: pieData,
 color: ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe', '#11998e', '#38ef7d']
 }]
 });
 }
 }
 catch (error) {
 console.error('Failed to load category chart:', error);
 }
};
const handleResize = () => {
 revenueChartInstance?.resize();
 orderChartInstance?.resize();
 productChartInstance?.resize();
 categoryChartInstance?.resize();
};
onMounted(async () => {
 window.addEventListener('resize', handleResize);
 try {
 loading.value = true;
 const res: any = await getAdminDashboard();
 if (res?.data) {
 stats.value = {
 todayRevenue: res.data.todayRevenue || 0,
 todayOrders: res.data.todayOrders || 0,
 totalOrders: res.data.totalOrders || 0,
 totalProducts: res.data.totalProducts || 0,
 totalUsers: res.data.totalUsers || 0,
 totalRevenue: res.data.totalRevenue || 0,
 pendingOrders: res.data.pendingOrders || 0,
 completedOrders: res.data.completedOrders || 0
 };
 }
 }
 catch (error) {
 console.error('Failed to load dashboard:', error);
 }
 finally {
 loading.value = false;
 }
 await new Promise(resolve => setTimeout(resolve, 100));
 initCharts();
 await loadRevenueChart();
 await loadOrderChart();
 await loadProductChart();
 await loadCategoryChart();
});
onUnmounted(() => {
 window.removeEventListener('resize', handleResize);
 revenueChartInstance?.dispose();
 orderChartInstance?.dispose();
 productChartInstance?.dispose();
 categoryChartInstance?.dispose();
});
</script>

<template>
  <div class="dashboard">
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>欢迎回来</h1>
        <p>今天是美好的一天，让我们来看看今天的运营数据吧</p>
      </div>
      <div class="welcome-decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <template v-else>
      <div class="stats-grid">
        <div class="stat-card primary">
          <div class="stat-icon">
            <span class="icon-text">¥</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.todayRevenue.toFixed(2) }}</div>
            <div class="stat-label">今日销售额</div>
            <div class="stat-trend up">
              <span class="trend-icon">↑</span>
              <span>较昨日</span>
            </div>
          </div>
        </div>

        <div class="stat-card success">
          <div class="stat-icon">
            <span class="icon-text">{{ stats.todayOrders }}</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayOrders }}</div>
            <div class="stat-label">今日订单</div>
            <div class="stat-trend">
              <span class="trend-text">待发货: {{ stats.pendingOrders }}</span>
            </div>
          </div>
        </div>

        <div class="stat-card warning">
          <div class="stat-icon">
            <span class="icon-text">{{ stats.totalUsers }}</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">用户总数</div>
            <div class="stat-trend">
              <span class="trend-text">活跃用户</span>
            </div>
          </div>
        </div>

        <div class="stat-card info">
          <div class="stat-icon">
            <span class="icon-text">{{ stats.totalProducts }}</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalProducts }}</div>
            <div class="stat-label">商品总数</div>
            <div class="stat-trend">
              <span class="trend-text">在售商品</span>
            </div>
          </div>
        </div>
      </div>

      <div class="secondary-stats">
        <div class="secondary-card">
          <div class="secondary-icon blue">
            <span>📦</span>
          </div>
          <div class="secondary-info">
            <div class="secondary-value">{{ stats.totalOrders }}</div>
            <div class="secondary-label">总订单数</div>
          </div>
        </div>

        <div class="secondary-card">
          <div class="secondary-icon green">
            <span>💰</span>
          </div>
          <div class="secondary-info">
            <div class="secondary-value">¥{{ stats.totalRevenue.toFixed(2) }}</div>
            <div class="secondary-label">总销售额</div>
          </div>
        </div>

        <div class="secondary-card">
          <div class="secondary-icon orange">
            <span>🚚</span>
          </div>
          <div class="secondary-info">
            <div class="secondary-value">{{ stats.pendingOrders }}</div>
            <div class="secondary-label">待发货订单</div>
          </div>
        </div>

        <div class="secondary-card">
          <div class="secondary-icon purple">
            <span>✅</span>
          </div>
          <div class="secondary-info">
            <div class="secondary-value">{{ stats.completedOrders }}</div>
            <div class="secondary-label">已完成订单</div>
          </div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="chart-card">
          <div class="chart-header">
            <h3>销售趋势</h3>
            <span class="chart-subtitle">近7天销售额</span>
          </div>
          <div ref="revenueChartRef" class="chart-content"></div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3>订单趋势</h3>
            <span class="chart-subtitle">近7天订单数</span>
          </div>
          <div ref="orderChartRef" class="chart-content"></div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3>热销商品</h3>
            <span class="chart-subtitle">TOP 5</span>
          </div>
          <div ref="productChartRef" class="chart-content"></div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3>分类分布</h3>
            <span class="chart-subtitle">商品分类占比</span>
          </div>
          <div ref="categoryChartRef" class="chart-content"></div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
}

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  color: #fff;
}

.welcome-content {
  position: relative;
  z-index: 2;
}

.welcome-content h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-content p {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.welcome-decoration {
  position: absolute;
  right: -20px;
  top: -20px;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 200px;
  height: 200px;
  right: 20px;
  top: -60px;
}

.circle-2 {
  width: 150px;
  height: 150px;
  right: 100px;
  top: 40px;
}

.circle-3 {
  width: 80px;
  height: 80px;
  right: 40px;
  top: 80px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.stat-card.primary .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.success .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.info .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-text {
  font-size: 20px;
  font-weight: 700;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.stat-trend.up {
  color: #52c41a;
}

.trend-icon {
  margin-right: 4px;
}

.secondary-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.secondary-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.secondary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.secondary-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.secondary-icon.blue {
  background: #e6f7ff;
}

.secondary-icon.green {
  background: #f6ffed;
}

.secondary-icon.orange {
  background: #fff7e6;
}

.secondary-icon.purple {
  background: #f9f0ff;
}

.secondary-value {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.secondary-label {
  font-size: 13px;
  color: #666;
  margin-top: 2px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.chart-subtitle {
  font-size: 12px;
  color: #999;
}

.chart-content {
  height: 280px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .secondary-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .secondary-stats {
    grid-template-columns: 1fr;
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
