import React from 'react';
import { Link } from 'react-router-dom';
import Image from '../../image/rob.png';
import IntlMessages from '../../components/utility/intlMessages';
import FourZeroFourStyleWrapper from './404.style';

class FourZeroFour extends React.Component {
  render() {
    return (
      <FourZeroFourStyleWrapper className="iso404Page">
        <div className="iso404Content">
          <h1>
            <IntlMessages id="page404.title" />
          </h1>
          <h3>
            <IntlMessages id="page404.subTitle" />
          </h3>
          <p>
            <IntlMessages id="page404.description" />
          </p>
          <button type="button">
            <Link to="/dashboard/my-videos">
              <IntlMessages id="page404.backButton" />
            </Link>
          </button>
        </div>

        <div className="iso404Artwork">
          <img alt="#" src={Image} />
        </div>
      </FourZeroFourStyleWrapper>
    );
  }
}

export default FourZeroFour;
