import {defineStore} from 'pinia';
import {computed, ref} from 'vue';
import {useUserStore} from './user';
import {StreamsApi} from "@/api/src/index.js";
import {callProtectedApi} from './authHelpers';

export const useStreamStore = defineStore('stream', () => {
    const userStore = useUserStore();

    const allStreams = ref([]);
    const liveStreams = ref([]);
    const currentUserStreams = ref([]);
    const currentStream = ref(null);
    const isLoading = ref(false);
    const error = ref(null);

    const enrichStream = (stream) => {
        if (!stream) return null;

        const enrichedStream = { ...stream };

        if (enrichedStream.tags && typeof enrichedStream.tags === 'object' && !Array.isArray(enrichedStream.tags)) {
            enrichedStream.tags = [];
        } else if (Array.isArray(enrichedStream.tags)) {
            enrichedStream.tags = [...enrichedStream.tags];
        } else {
            enrichedStream.tags = [];
        }

        enrichedStream.getFormattedStartTime = function () {
            if (!this.startedAt) return 'Нет данных';
            try {
                const date = new Date(this.startedAt);
                return date.toLocaleString('ru-RU');
            } catch (error) {
                return 'Нет данных';
            }
        };

        enrichedStream.getDuration = function () {
            if (!this.startedAt) return 'Нет данных';

            const start = new Date(this.startedAt);
            const end = this.endedAt ? new Date(this.endedAt) : new Date();

            const durationMs = end - start;
            const hours = Math.floor(durationMs / (1000 * 60 * 60));
            const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));

            return `${hours}ч ${minutes}м`;
        };

        return enrichedStream;
    };

    const hasActiveStream = computed(() => {
        return currentUserStreams.value.some(stream => stream.isLive);
    });

    const activeStream = computed(() => {
        return currentUserStreams.value.find(stream => stream.isLive) || null;
    });

    async function fetchAllStreams() {
        isLoading.value = true;
        error.value = null;

        try {
            const streamsService = new StreamsApi();
            const streams = await streamsService.getAllStreams();
            allStreams.value = streams.map(stream => enrichStream(stream));
        } catch (err) {
            console.error('Ошибка при загрузке стримов:', err.message);
            error.value = 'Не удалось загрузить стримы';
            throw new Error(err.response?.status === 401 ? 'Unauthorized' : 'Failed to fetch streams');
        } finally {
            isLoading.value = false;
        }
    }

    async function fetchLiveStreams() {
        isLoading.value = true;
        error.value = null;

        try {
            const streamsService = new StreamsApi();
            const streams = await streamsService.getLiveStreams();
            liveStreams.value = streams.map(stream => enrichStream(stream));
        } catch (err) {
            console.error('Ошибка при загрузке активных стримов:', err.message);
            error.value = 'Не удалось загрузить активные стримы';
            throw new Error(err.response?.status === 401 ? 'Unauthorized' : 'Failed to fetch live streams');
        } finally {
            isLoading.value = false;
        }
    }

    async function fetchCurrentUserStreams() {
        if (!userStore.isAuthenticated || !userStore.user?.id) {
            error.value = 'Необходимо авторизоваться';
            throw new Error('Unauthorized');
        }


        if (!userStore.user.id || userStore.user.id === 'undefined') {
            console.error('User ID is invalid:', userStore.user.id);
            error.value = 'ID пользователя не найден';
            throw new Error('Invalid user ID');
        }

        isLoading.value = true;
        error.value = null;

        try {
            const streamsService = new StreamsApi();

            const userId = String(userStore.user.id);

            const streams = await callProtectedApi(() => streamsService.getStreamsByUser(userId));
            currentUserStreams.value = streams.map(stream => enrichStream(stream));
        } catch (err) {
            console.error('Ошибка при загрузке стримов пользователя:', err.message);
            error.value = 'Не удалось загрузить ваши стримы';
            throw new Error(err.message || 'Failed to fetch user streams');
        } finally {
            isLoading.value = false;
        }
    }


    async function fetchStreamById(streamId) {
        if (!streamId) {
            error.value = 'ID стрима не указан';
            throw new Error('Stream ID is required');
        }

        isLoading.value = true;
        error.value = null;

        try {
            const streamsService = new StreamsApi();
            const stream = await streamsService.getStreamById(streamId);
            currentStream.value = enrichStream(stream);
        } catch (err) {
            console.error('Ошибка при загрузке стрима:', err.message);
            if (err.response?.status === 403) {
                error.value = 'Доступ запрещен';
            } else if (err.response?.status === 404) {
                error.value = 'Стрим не найден';
            } else {
                error.value = 'Не удалось загрузить стрим';
            }
            currentStream.value = null;
            throw new Error(err.response?.status === 404 ? 'Stream not found' : 'Failed to fetch stream');
        } finally {
            isLoading.value = false;
        }
    }


    async function createStream(formData) {
        const streamsService = new StreamsApi();

        const title = formData.get("title");
        const description = formData.get("description") || '';
        const thumbnailUrl = formData.get("thumbnailUrl");

        return await callProtectedApi(() =>
            streamsService.createStream(title, {
                description,
                thumbnailUrl
            })
        );
    }

    async function updateStream(formData) {
        if (!userStore.isAuthenticated) {
            error.value = 'Необходимо авторизоваться';
            throw new Error('Unauthorized');
        }

        isLoading.value = true;
        error.value = null;

        try {
            const streamsService = new StreamsApi();
            const title = formData.get("title");
            const description = formData.get("description") || '';
            const thumbnailUrl = formData.get("thumbnailUrl");

            const updatedStream = await callProtectedApi(() => streamsService.updateStream(title, {
                description,
                thumbnailUrl
            }));
            const enrichedStream = enrichStream(updatedStream);

            const index = currentUserStreams.value.findIndex(s => s.id === updatedStream.id);
            if (index !== -1) {
                currentUserStreams.value[index] = enrichedStream;
            }

            if (currentStream.value && currentStream.value.id === updatedStream.id) {
                currentStream.value = enrichedStream;
            }
            return enrichedStream;
        } catch (err) {
            console.error('Ошибка при обновлении стрима:', err.message);
            if (err.response?.status === 401) {
                error.value = 'Требуется авторизация';
            } else if (err.response?.status === 403) {
                error.value = 'Доступ запрещен';
            } else if (err.response?.status === 404) {
                error.value = 'Стрим не найден';
            } else {
                error.value = 'Не удалось обновить стрим';
            }
            throw new Error(err.message || 'Failed to update stream');
        } finally {
            isLoading.value = false;
        }
    }

    async function endStream() {
        if (!userStore.isAuthenticated) {
            error.value = 'Необходимо авторизоваться';
            throw new Error('Unauthorized');
        }

        isLoading.value = true;
        error.value = null;

        try {
            const streamsService = new StreamsApi();
            await callProtectedApi(() => streamsService.endStream());
            await callProtectedApi(() => fetchCurrentUserStreams());
        } catch (err) {
            console.error('Ошибка при завершении стрима:', err.message);
            if (err.response?.status === 401) {
                error.value = 'Требуется авторизация';
            } else if (err.response?.status === 403) {
                error.value = 'Доступ запрещен';
            } else if (err.response?.status === 404) {
                error.value = 'Стрим не найден';
            } else {
                error.value = 'Не удалось завершить стрим';
            }
            throw new Error(err.message || 'Failed to end stream');
        } finally {
            isLoading.value = false;
        }
    }

    async function searchStreams(query) {
        if (!query || query.trim().length === 0) {
            return [];
        }

        isLoading.value = true;
        error.value = null;

        try {
            const streamsService = new StreamsApi();
            const streams = await streamsService.searchStreams(query);
            return streams.map(stream => enrichStream(stream));
        } catch (err) {
            console.error('Ошибка при поиске стримов:', err.message);
            error.value = 'Не удалось выполнить поиск';
            throw new Error(err.message || 'Failed to search streams');
        } finally {
            isLoading.value = false;
        }
    }

    function setActiveStream(stream) {
        if (!stream) return;

        const enrichedStream = enrichStream(stream);

        const index = currentUserStreams.value.findIndex(s => s.id === stream.id);
        if (index !== -1) {
            currentUserStreams.value[index] = enrichedStream;
        } else {
            if (enrichedStream.isLive) {
                const existingActiveIndex = currentUserStreams.value.findIndex(s => s.isLive && s.id !== stream.id);
                if (existingActiveIndex !== -1) {
                    currentUserStreams.value[existingActiveIndex].isLive = false;
                }
            }
            currentUserStreams.value.push(enrichedStream);
        }
    }

    return {
        allStreams,
        liveStreams,
        currentUserStreams,
        currentStream,
        isLoading,
        error,
        hasActiveStream,
        activeStream,
        fetchAllStreams,
        fetchLiveStreams,
        fetchCurrentUserStreams,
        fetchStreamById,
        createStream,
        updateStream,
        endStream,
        searchStreams,
        setActiveStream,
    };
});