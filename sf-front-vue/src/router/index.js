import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '@/stores/user'

const requireAuth = async (to, from, next) => {
  const userStore = useUserStore()
  
  if (!userStore.isAuthenticated) {
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      beforeEnter: requireAuth
    },
    {
      path: '/stream/:id',
      name: 'stream',
      component: () => import('../views/StreamView.vue')
    },
    {
      path: '/streams/dashboard',
      name: 'stream-dashboard',
      component: () => import('../views/StreamDashboardView.vue'),
    },
    {
      path: '/create-stream',
      name: 'create-stream',
      component: () => import('../views/CreateStreamView.vue'),
      beforeEnter: requireAuth
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/privacy-policy',
      name: 'privacy-policy',
      component: () => import('../views/PrivacyPolicyView.vue')
    },
    {
      path: '/test-stream-card',
      name: 'test-stream-card',
      component: () => import('../components/stream/TestStreamCard.vue')
    }
  ]
})

export default router 