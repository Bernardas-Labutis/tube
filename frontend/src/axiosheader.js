
import axios from 'axios';

(function() {
    var token = localStorage.id_token;
    if (token != null) {
        axios.defaults.headers.common['Authorization'] = token;
    } else {
        //axios.defaults.headers.common['Authorization'] = null;
        delete axios.defaults.headers.common['Authorization'];
  
    }
  })();

  export default function checkforHeader() {
    var token = localStorage.id_token;
    if (token != null) {
        axios.defaults.headers.common['Authorization'] = token;
    } else {
        //axios.defaults.headers.common['Authorization'] = null;
        delete axios.defaults.headers.common['Authorization'];
  
    }
  }