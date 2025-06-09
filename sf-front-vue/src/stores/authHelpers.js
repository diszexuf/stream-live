import { ApiClient } from "@/api/src/index.js";

export function setAuthToken(token) {
    if (token) {
        ApiClient.instance.authentications.bearerAuth.accessToken = token;
    } else {
        ApiClient.instance.authentications.bearerAuth.accessToken = null;
    }
}

export async function callProtectedApi(apiMethod) {
    if (!ApiClient.instance.authentications.bearerAuth.accessToken) {
        const storedToken = localStorage.getItem('token');
        if (storedToken) {
            setAuthToken(storedToken);
        } else {
            throw new Error('Unauthorized: No token found');
        }
    }

    try {
        return await apiMethod();
    } catch (error) {
        console.error('Ошибка при выполнении защищённого запроса:', error);
        if (error?.response?.status === 401) {
            throw new Error('Unauthorized: Невалидный или истёкший токен');
        } else if (error?.response?.status === 403) {
            throw new Error('Forbidden: Доступ запрещён');
        } else {
            throw new Error('API request failed: ' + (error.message || 'Unknown error'));
        }
    }
}