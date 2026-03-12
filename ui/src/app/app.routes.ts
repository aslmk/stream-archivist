import { Routes } from '@angular/router';
import {Home} from './pages/home/home';
import {LoginComponent} from './pages/login/login';

export const routes: Routes = [
  {
    path: '',
    component: Home
  },
  {
    path: 'auth/login',
    component: LoginComponent
  }
];
