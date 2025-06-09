import { ApiClient } from "@/api/src/index.js";

export function setAuthToken(token) {
    if (token) {
        ApiClient.instance.authentications.bearerAuth.accessToken = token;
        localStorage.setItem('token', token);
    } else {
        ApiClient.instance.authentications.bearerAuth.accessToken = null;
        localStorage.removeItem('token');
    }
}

export function getAuthToken() {
    return localStorage.getItem('token');
}

export function isTokenValid() {
    const token = getAuthToken();
    if (!token) return false;
    return true;
}

export async function callProtectedApi(apiMethod) {
    if (!ApiClient.instance.authentications.bearerAuth.accessToken) {
        const storedToken = getAuthToken();
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
            setAuthToken(null);
            throw new Error('Unauthorized: Невалидный или истёкший токен');
        } else if (error?.response?.status === 403) {
            throw new Error('Forbidden: Доступ запрещён');
        } else {
            throw new Error('API request failed: ' + (error.message || 'Unknown error'));
        }
    }
}
