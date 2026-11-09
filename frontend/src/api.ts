import axios from "axios";
export interface User {
  fullName: string;
  email: string;
  phoneNumber: string;
  createdAt: string;
}
export interface Transaction {
  transactionId: number;
  tuitionId: number;
  amount: number;
  status: string;
  createdAt: string;
}
export interface Tuition {
  tuitionId: number;
  studentId: string;
  studentName: string;
  status: string;
  amount: number;
}
export interface Otp {
  paymentId: string;
  email: string;
  expiresAt: string;
  expiresInSeconds: number;
  message: string;
}
let token = "";
export const setToken = (value: string) => {
  token = value;
};
const client = axios.create({ timeout: 20000 });
client.interceptors.request.use((config) => {
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && token)
      window.dispatchEvent(new Event("session-expired"));
    return Promise.reject(error);
  },
);
const base = {
  auth: import.meta.env.VITE_AUTH_URL || "/gateway/auth",
  user: import.meta.env.VITE_USER_URL || "/gateway/user",
  balance: import.meta.env.VITE_BALANCE_URL || "/gateway/balance",
  tuition: import.meta.env.VITE_TUITION_URL || "/gateway/tuition",
  payment: import.meta.env.VITE_PAYMENT_URL || "/gateway/payment",
};
export const api = {
  login: async (username: string, password: string) =>
    (
      await client.post<{
        token: string;
        user: { userId: string; username: string };
      }>(`${base.auth}/login`, { username, password })
    ).data,
  me: async () => (await client.get<User>(`${base.user}/api/users/me`)).data,
  balance: async () =>
    (await client.get<{ balance: number }>(`${base.balance}/api/balances/me`))
      .data,
  history: async () =>
    (
      await client.get<Transaction[]>(
        `${base.payment}/api/payment-transactions/history`,
      )
    ).data,
  tuition: async (id: string) =>
    (
      await client.get<Tuition>(
        `${base.tuition}/api/tuitions/${encodeURIComponent(id)}`,
      )
    ).data,
  requestOtp: async (studentId: string) =>
    (
      await client.post<Otp>(`${base.payment}/api/payments/request-otp`, {
        studentId,
        acceptedTerms: true,
      })
    ).data,
  verify: async (paymentId: string, code: string) => {
    await client.post(`${base.payment}/api/payments/verify`, {
      paymentId,
      code,
    });
  },
};
export function errorMessage(error: unknown): string {
  if (!axios.isAxiosError(error))
    return "Không thể xử lý dữ liệu. Vui lòng thử lại.";
  if (!error.response)
    return "Không thể kết nối dịch vụ. Kiểm tra kết nối và thử lại.";
  const messages: Record<string, string> = {
    "Insufficient balance to pay tuition.":
      "Số dư không đủ để thanh toán toàn bộ học phí.",
    "Tuition has already been paid.": "Học phí đã được thanh toán.",
    "Tuition is currently being processed.":
      "Học phí đang được xử lý. Vui lòng kiểm tra lại sau.",
    "Terms and conditions must be accepted.":
      "Bạn cần chấp nhận điều khoản thanh toán.",
  };
  const body = error.response.data;
  if (typeof body === "string" && messages[body]) return messages[body];
  if (error.response.status === 401)
    return "Thông tin đăng nhập không đúng hoặc phiên đăng nhập đã hết hạn.";
  if (error.response.status === 404) return "Không tìm thấy thông tin yêu cầu.";
  if (error.response.status >= 500)
    return "Dịch vụ tạm thời chưa sẵn sàng. Vui lòng thử lại sau.";
  return "Yêu cầu chưa được chấp nhận. Nếu đang xác nhận thanh toán, hãy kiểm tra học phí và lịch sử trước khi tạo giao dịch mới. OTP có thể sai, hết hạn hoặc đã sử dụng.";
}
