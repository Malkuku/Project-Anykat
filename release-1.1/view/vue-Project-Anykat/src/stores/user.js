import {defineStore} from 'pinia';
import {ref} from 'vue';

export const useUserStore = defineStore('user', () => {
        const id = ref(null);
        const username = ref('');
        const token = ref('');
        const adminToken = ref('');
        const name = ref('');
        const role = ref(null);

        const setUserInfo = (userInfo) => {
            id.value = userInfo.id;
            username.value = userInfo.username;
            token.value = userInfo.token;
            adminToken.value = userInfo.adminToken;
            name.value = userInfo.name;
            role.value = userInfo.role;
        };

        const clearUserInfo = () => {
            id.value = null;
            username.value = '';
            token.value = '';
            adminToken.value = '';
            name.value = '';
            role.value = null;
        };

        const isAuthenticated = () => {
            return !!token.value;
        };

        return {
            id,
            username,
            token,
            adminToken,
            role,
            setUserInfo,
            clearUserInfo,
            isAuthenticated
        };
    },
    {
        persist: {
            enabled: true, // 启用持久化
            storage: sessionStorage,
        },
    }
);
