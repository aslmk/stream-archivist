import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {LoginComponent} from './pages/login/login';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoginComponent],
  templateUrl: './app.html',
  standalone: true,
  styleUrl: './app.css'
})
export class App {}
