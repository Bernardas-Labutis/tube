import React from 'react';
import { Link } from 'react-router-dom';
import { connect } from 'react-redux';
import Input from '../../components/uielements/input';
import Button from '../../components/uielements/button';
import IntlMessages from '../../components/utility/intlMessages';
import SignUpStyleWrapper from './signup.style';
import authAction from '../../redux/auth/actions';
import axios from 'axios';

const { login } = authAction;

class SignUp extends React.Component {
  state = {
    redirectToReferrer: false,
    firstName: '',
    lastName: '',
    username: '',
    password: '',
    passwordConfirm:'',
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

  registerCall() {
    if(this.state.password == this.state.passwordConfirm){
      axios.post('http://localhost:8080/api/v1/registration', {
        "firstName": this.state.firstName,
        "lastName": this.state.lastName,
        "email": this.state.username,
        "password": this.state.password
      }).then((response) => {
        console.log(response)
      });
    } else {
      this.state.error = "Passwords don't match"
    }
    
  }

  handleRegistration = () => {
    const { login } = this.props;
    this.registerCall();
    //this.props.history.push('/signin');
  };
  render() {
    return (
      <SignUpStyleWrapper className="isoSignUpPage">
        <div className="isoSignUpContentWrapper">
          <div className="isoSignUpContent">
            <div className="isoLogoWrapper">
              <Link to="/dashboard">
                <IntlMessages id="page.signUpTitle" />
              </Link>
            </div>

            <div className="isoSignUpForm">
            <div className="isoInputWrapper">
                <Input size="large" placeholder="First Name" value={this.state.firstName} onChange={evt => this.setState({firstName: evt.target.value})}/>
              </div>

              <div className="isoInputWrapper">
                <Input size="large" placeholder="Last Name" value={this.state.lastName} onChange={evt => this.setState({lastName: evt.target.value})}/>
              </div>

              <div className="isoInputWrapper">
                <Input size="large" placeholder="Email" value={this.state.username} onChange={evt => this.setState({username: evt.target.value})}/>
              </div>

              <div className="isoInputWrapper">
                <Input onChange={evt => this.setState({password: evt.target.value})} value={this.state.password} size="large" type="password" placeholder="Password" />
              </div>

              <div className="isoInputWrapper">
                <Input
                  size="large"
                  type="password"
                  placeholder="Confirm Password"
                  onChange={evt => this.setState({passwordConfirm: evt.target.value})}
                  value={this.state.passwordConfirm}
                />
              </div>
              <div className="isoInputWrapper">
                <Button type="primary" onClick={this.handleRegistration}>
                  <IntlMessages id="page.signUpButton" />
                </Button>
              </div>
              <div className="isoInputWrapper isoCenterComponent isoHelperWrapper">
                <Link to="/signin">
                  <IntlMessages id="page.signUpAlreadyAccount" />
                </Link>
              </div>
            </div>
          </div>
        </div>
      </SignUpStyleWrapper>
    );
  }
}

export default connect(
  state => ({
    isLoggedIn: state.Auth.get('idToken') !== null ? true : false,
  }),
  { login }
)(SignUp);
