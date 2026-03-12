import { Pipe, PipeTransform } from '@angular/core';
import {environment} from '../../../environments/environments';

@Pipe({
  name: 'imgUrl',
  standalone: true
})
export class ImgUrlPipe implements PipeTransform {

  transform(value: string | null): string {
    if (!value) return environment.twitchIconUrl;
    return value
  }

}
