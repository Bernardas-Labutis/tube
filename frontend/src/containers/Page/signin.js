import React, { Component } from 'react';
import { Link, Redirect } from 'react-router-dom';
import { connect } from 'react-redux';
import Input from '../../components/uielements/input';
import Button from '../../components/uielements/button';
import authAction from '../../redux/auth/actions';
import IntlMessages from '../../components/utility/intlMessages';
import SignInStyleWrapper from './signin.style';
import axios from 'axios';

const { login } = authAction;

class SignIn extends Component {
  state = {
    redirectToReferrer: false,
    username: '',
    password: '',
    error: ''
  };
  componentWillReceiveProps(nextProps) {
    if (
      this.props.isLoggedIn !== nextProps.isLoggedIn &&
      nextProps.isLoggedIn === true
    ) {
      this.setState({ redirectToReferrer: true });
    }
  }

  loginCall() {
    axios.post('/login', {
      "username": this.state.username,
      "password": this.state.password
    }).then((response) =>{
      if(response.status == 200){
        localStorage.setItem('id_token', response.headers.authorization);
        this.props.history.push('/dashboard/my-videos');

      } else {
        localStorage.clear();
        this.setState({error: "Somethings wrong"});
      }
    });

    
  }


  handleLogin = () => {
    const { login } = this.props;
    this.loginCall();
    //login(this.state.username, this.state.password);
    //this.props.history.push('/dashboard');//TODO need to check somehow if has token from local storage and push dashboard or signin
    
  };
  render() {
    const from = { pathname: '/dashboard' };
    const { redirectToReferrer } = this.state;

    if (redirectToReferrer) {
      return <Redirect to={from} />;
    }
    return (
      <SignInStyleWrapper className="isoSignInPage">
        <div className="isoLoginContentWrapper">
          <div className="isoLoginContent">
            <div className="isoLogoWrapper">
              <Link to="/dashboard">
                <IntlMessages id="page.signInTitle" />
              </Link>
            </div>

            <div className="isoSignInForm">
              <div className="isoInputWrapper">
                <Input size="large" placeholder="Username" value={this.state.username} onChange={evt => this.setState({username: evt.target.value})}/>
              </div>

              <div className="isoInputWrapper">
                <Input value={this.state.password} size="large" type="password" placeholder="Password" onChange={evt => this.setState({password: evt.target.value})}/>
              </div>

              <div className="isoInputWrapper isoLeftRightComponent">
                <Button type="primary" onClick={this.handleLogin}>
                  <IntlMessages id="page.signInButton" />
                </Button>
              </div>
              <p>
                {this.state.error}
              </p>
              <p className="isoHelperText">
                <IntlMessages id="page.signInPreview" />
              </p>
              <div className="isoCenterComponent isoHelperWrapper">
                <Link to="/signup">
                  <IntlMessages id="page.signInCreateAccount" />
                </Link>
              </div>
            </div>
          </div>
        </div>
      </SignInStyleWrapper>
    );
  }
}

export default connect(
  state => ({
    isLoggedIn: state.Auth.get('idToken') !== null ? true : false,
  }),
  { login }
)(SignIn);
