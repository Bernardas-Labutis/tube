import { all, takeEvery, put, fork } from 'redux-saga/effects';
import { push } from 'react-router-redux';
import { clearToken } from '../../helpers/utility';
import actions from './actions';
import axios from 'axios';
import '../../axiosheader';

const fakeApiCall = true; // auth0 or express JWT

// export function* loginRequest(){
//   yield takeEvery('LOGIN_REQUEST', function*(response) {
//     if (fakeApiCall) {
//       yield put({
//         type: actions.LOGIN_SUCCESS,
//         token: 'secret token',
//         profile: 'Profile'
//       });
//     } else {
//       yield put({ type: actions.LOGIN_ERROR });
//     }
//   });
// }


export function* loginRequest() {
  yield takeEvery('LOGIN_REQUEST', function*(payload) {
    console.log(payload);
    const response = yield axios.post('login', {
      username: payload.username,
      password: payload.password
    })
    console.log(response);
    if (response.status == "200") {
      console.log(response.headers);
      yield put({
        type: actions.LOGIN_SUCCESS,
        token: response.headers.authorization,
        profile: 'Profile'
      });
    } else {
      console.log(payload);
      yield put({ type: actions.LOGIN_ERROR });
    }
  });
}

export function* loginSuccess() {
  yield takeEvery(actions.LOGIN_SUCCESS, function*(payload) {
    yield localStorage.setItem('id_token', payload.token);
    yield put(push('dashboard/my-videos'));
  });
}

export function* loginError() {
  yield takeEvery(actions.LOGIN_ERROR, function*() {});
}

export function* logout() {
  yield takeEvery(actions.LOGOUT, function*() {
    clearToken();
    yield put(push('/'));
  });
}
export default function* rootSaga() {
  yield all([
    fork(loginRequest),
    fork(loginSuccess),
    fork(loginError),
    fork(logout)
  ]);
}
