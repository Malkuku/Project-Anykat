<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { User, Avatar } from '@element-plus/icons-vue';

const activePanel = ref(null);
const loginForm = ref({
  username: '',
  password: '',
  role: '' // 'student' or 'teacher'
});

const showLoginForm = (role) => {
  loginForm.value.role = role;
  activePanel.value = role;
};

const handleLogin = () => {
  console.log('登录信息:', loginForm.value);
  // 这里可以添加统一的登录逻辑
  // 根据 loginForm.value.role 区分学生/教师
};

// 粒子效果代码
const initParticles = () => {
  const canvas = document.createElement('canvas');
  canvas.style.position = 'fixed';
  canvas.style.top = '0';
  canvas.style.left = '0';
  canvas.style.zIndex = '-1';
  canvas.style.width = '100%';
  canvas.style.height = '100%';
  document.body.appendChild(canvas);

  const ctx = canvas.getContext('2d');
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  const particles = [];
  const particleCount = Math.floor(window.innerWidth * window.innerHeight / 10000);

  // 创建粒子
  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      size: Math.random() * 3 + 1,
      speedX: Math.random() * 1 - 0.5,
      speedY: Math.random() * 1 - 0.5,
      color: `rgba(64, 158, 255, ${Math.random() * 0.5 + 0.1})`
    });
  }

  // 绘制粒子
  const drawParticles = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (let i = 0; i < particles.length; i++) {
      const p = particles[i];

      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fillStyle = p.color;
      ctx.fill();

      // 更新位置
      p.x += p.speedX;
      p.y += p.speedY;

      // 边界检查
      if (p.x < 0 || p.x > canvas.width) p.speedX *= -1;
      if (p.y < 0 || p.y > canvas.height) p.speedY *= -1;

      // 绘制连线
      for (let j = i + 1; j < particles.length; j++) {
        const p2 = particles[j];
        const distance = Math.sqrt(Math.pow(p.x - p2.x, 2) + Math.pow(p.y - p2.y, 2));

        if (distance < 100) {
          ctx.beginPath();
          ctx.strokeStyle = `rgba(64, 158, 255, ${1 - distance / 100})`;
          ctx.lineWidth = 0.5;
          ctx.moveTo(p.x, p.y);
          ctx.lineTo(p2.x, p2.y);
          ctx.stroke();
        }
      }
    }

    requestAnimationFrame(drawParticles);
  };

  // 处理窗口大小变化
  const handleResize = () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  };

  window.addEventListener('resize', handleResize);
  drawParticles();

  // 返回清理函数
  return () => {
    window.removeEventListener('resize', handleResize);
    document.body.removeChild(canvas);
  };
};

// 初始化粒子效果
onMounted(() => {
  const cleanup = initParticles();
  onUnmounted(cleanup);
});
</script>

<template>
  <div class="home-container">
    <div class="platform-title">
      <h1>Anykat实训平台</h1>
      <p class="subtitle">创新 · 实践 · 成长</p>
    </div>

    <div class="panels-container">
      <!-- 学生面板 -->
      <div
          class="panel student-panel"
          :class="{ 'active': activePanel === 'student' }"
          @click="showLoginForm('student')"
      >
        <div class="panel-content">
          <h2>学生入口</h2>
          <div class="icon">
            <el-icon size="60"><User /></el-icon>
          </div>
          <div class="waves"></div>
        </div>

        <transition name="slide">
          <div class="login-form" v-show="activePanel === 'student'">
            <el-form :model="loginForm" label-position="top">
              <el-form-item label="学号">
                <el-input v-model="loginForm.username" placeholder="请输入学号" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
              </el-form-item>
              <el-button type="primary" @click="handleLogin" class="login-btn">登录</el-button>
            </el-form>
          </div>
        </transition>
      </div>

      <!-- 教师面板 -->
      <div
          class="panel teacher-panel"
          :class="{ 'active': activePanel === 'teacher' }"
          @click="showLoginForm('teacher')"
      >
        <div class="panel-content">
          <h2>教师入口</h2>
          <div class="icon">
            <el-icon size="60"><Avatar /></el-icon>
          </div>
          <div class="waves"></div>
        </div>

        <transition name="slide">
          <div class="login-form" v-show="activePanel === 'teacher'">
            <el-form :model="loginForm" label-position="top">
              <el-form-item label="工号">
                <el-input v-model="loginForm.username" placeholder="请输入工号" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
              </el-form-item>
              <el-button type="primary" @click="handleLogin" class="login-btn">登录</el-button>
            </el-form>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: rgba(245, 247, 250, 0.7);
  position: relative;
  z-index: 1;
}

.platform-title {
  margin-bottom: 50px;
  text-align: center;
  animation: fadeIn 1s ease;
}

.platform-title h1 {
  font-size: 42px;
  color: #409eff;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  font-size: 16px;
  color: #606266;
  letter-spacing: 2px;
}

.panels-container {
  display: flex;
  width: 80%;
  max-width: 900px;
  height: 400px;
  gap: 20px;
  animation: fadeInUp 0.8s ease;
}

.panel {
  flex: 1;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  display: flex;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.panel:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  transition: all 0.5s ease;
  position: relative;
  z-index: 2;
}

.panel h2 {
  color: white;
  margin-bottom: 30px;
  font-size: 24px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.icon {
  color: white;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  transition: transform 0.3s ease;
}

.panel:hover .icon {
  transform: scale(1.1);
}

.student-panel {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.teacher-panel {
  background: linear-gradient(135deg, #337ecc, #409eff);
}

.login-form {
  width: 300px;
  padding: 30px;
  background-color: white;
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.1);
  z-index: 3;
}

.slide-enter-active, .slide-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-enter-from, .slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.active .panel-content {
  transform: translateX(-150px);
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}

/* 波浪效果 */
.waves {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='rgba(255,255,255,0.2)' d='M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 1;
  animation: wave 8s linear infinite;
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes wave {
  0% { background-position-x: 0; }
  100% { background-position-x: 1440px; }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .panels-container {
    flex-direction: column;
    height: auto;
  }

  .panel {
    height: 200px;
    margin-bottom: 20px;
  }

  .login-form {
    width: 100%;
    position: absolute;
    right: 0;
    height: 100%;
  }

  .active .panel-content {
    transform: translateX(-50%);
  }

  .platform-title h1 {
    font-size: 32px;
  }
}
</style>