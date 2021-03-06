import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import { ConnectedRouter } from 'react-router-redux';
import { connect } from 'react-redux';

import App from './containers/App/App';
import asyncComponent from './helpers/AsyncFunc';
import Auth0 from './helpers/auth0';
import axios from 'axios';
import SharedVideo from './containers/SharedVideo/sharedvideo'
const RestrictedRoute = ({ component: Component, isLoggedIn, ...rest }) => (
  <Route
    {...rest}
    render={props => isLoggedIn
      ? <Component {...props} />
      : <Redirect
          to={{
            pathname: '/signin',
            state: { from: props.location },
          }}
        />}
  />
);
const PublicRoutes = ({ history, isLoggedIn }) => {
  return (
    <ConnectedRouter history={history}>
      <div>
        <Route
          exact
          path={'/'}
          component={asyncComponent(() => import('./containers/Page/signin'))}
        />
        <Route
          exact
          path={'/404'}
          component={asyncComponent(() => import('./containers/Page/404'))}
        />
        <Route
          exact
          path={'/500'}
          component={asyncComponent(() => import('./containers/Page/500'))}
        />
        <Route
          exact
          path={'/signin'}
          component={asyncComponent(() => import('./containers/Page/signin'))}
        />
        <Route
          exact
          path={'/signup'}
          component={asyncComponent(() => import('./containers/Page/signup'))}
        />
        <Route
          path="/auth0loginCallback"
          render={props => {
            Auth0.handleAuthentication(props);
          }}
        />
        <RestrictedRoute
          path="/dashboard"
          component={App}
          isLoggedIn={isLoggedIn}
        />
				<Route
					exact
					path={`/share/:uuid`}
					component={SharedVideo}
				/>
      </div>
    </ConnectedRouter>
  );
};

export default connect(state => ({
  
  isLoggedIn: state.Auth.get('idToken') !== null,
}))(PublicRoutes);

(function() {
  var token = localStorage.id_token;
  if (token != null) {
      axios.defaults.headers.common['Authorization'] = token;
  } else {
      //axios.defaults.headers.common['Authorization'] = null;
      delete axios.defaults.headers.common['Authorization'];

  }
})();