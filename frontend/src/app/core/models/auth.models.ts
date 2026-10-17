export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthenticatedUserResponse;
}

export interface AuthenticatedUserResponse {
  userId: string;
  username: string;
  roles: string[];
  firstName: string;
  lastName: string;
}
