// 格式化日期时间
export const formatDateTime = (dateStr) => {
    if (!dateStr) return '';

    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
};

// 格式化日期（YYYY-MM-DD）
export const formatDate = (dateStr) => {
    if (!dateStr) return '';

    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
};


// 计算剩余时间（倒计时）
export const getCountdown = (endTime) => {
    if (!endTime) return '';

    const now = new Date();
    const end = new Date(endTime);
    const diff = end - now;

    if (diff <= 0) return '已截止';

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);

    let result = [];
    if (days > 0) result.push(`${days}天`);
    if (hours > 0) result.push(`${hours}小时`);
    if (minutes > 0) result.push(`${minutes}分钟`);
    result.push(`${seconds}秒`);

    return result.join('');
};