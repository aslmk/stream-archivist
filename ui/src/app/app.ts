import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {LoginComponent} from './pages/login/login';
import {Card} from './components/card/card';
import {Home} from './pages/home/home';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoginComponent, Card, Home],
  templateUrl: './app.html',
  standalone: true,
  styleUrl: './app.css'
})
export class App {}
