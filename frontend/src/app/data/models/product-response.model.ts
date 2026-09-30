import {RatingResponse} from './rating-response.model';

export interface ProductResponse {

  id: number;
  title: string;
  price: number;
  description: string;
  category: string;
  rating: RatingResponse;
  note: string;


}
