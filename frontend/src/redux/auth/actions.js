const authActons = {
  LOGIN_REQUEST: 'LOGIN_REQUEST',
  LOGOUT: 'LOGOUT',
  LOGIN_SUCCESS: 'LOGIN_SUCCESS',
  LOGIN_ERROR: 'LOGIN_ERROR',
  login: (username, password) => ({
    type: authActons.LOGIN_REQUEST,
    username: username,
    password: password
  }),
  logout: () => ({
    type: authActons.LOGOUT,
  }),
};
export default authActons;
