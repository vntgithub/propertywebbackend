package vntrieu.train.bdsbackend.service;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;;
import vntrieu.train.bdsbackend.RabbitMQ.RabbitMQMessage;
import vntrieu.train.bdsbackend.RabbitMQ.RabbitMQSender;
import vntrieu.train.bdsbackend.dto.AddressDTO;
import vntrieu.train.bdsbackend.dto.ContactDTO;
import vntrieu.train.bdsbackend.model.*;
import vntrieu.train.bdsbackend.repository.ContactRepository;
import vntrieu.train.bdsbackend.repository.FilterRepository;
import vntrieu.train.bdsbackend.repository.ImageRepository;
import vntrieu.train.bdsbackend.repository.ProductRepository;

@Service
public class ProductService {

  @Autowired
  private  ProductRepository productRepository;

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private  ImageRepository imageRepository;

  @Autowired
  private  FilterRepository filterRepository;

  @Autowired
  private  RabbitMQSender rabbitMQSender;



  public Long getNumberOfPage(){
    Long numberOfProduct = productRepository.getNumberOfPage();
    Long n =  numberOfProduct/10;
    Short m = (short) (numberOfProduct%10);
    if(m != 0)
      return n + 1;
    return n;
  }

  public List<Product> getProduct(Pageable page){
    return productRepository.findAllBy(page);
  }

  public List<Product> getByUserId(Long userId){
    return productRepository.getByUserId(userId);
  }


  public String getPriceRangeString(Long price) {
    if(price < 500000000)
      return "0";
    if(price >= 500000000 && price < 1000000000)
      return "1";
    if(price >=1000000000 && price < 1500000000)
      return "2";
    if(price >=1500000000 && price <2000000000)
      return "3";

    return "4";
  }
  public Product add(Product p){

    //Add product
    Product newProduct =  productRepository.save(p);
    Collection<Image> listImage =  newProduct.getImages();
    for(Image i : listImage){
      i.setProduct(newProduct);
      imageRepository.save(i);
    }

//    Get list filter from product information
    String streetId = p.getAddress().getStreet().getId().toString();
    String wardId = p.getAddress().getWard().getId().toString();
    String priceRange = getPriceRangeString(p.getPrice());
    List<Contact> list = contactRepository.searchContact(streetId, wardId, priceRange);
    for(Contact c : list){
      RabbitMQMessage  mess = new RabbitMQMessage(
              c.getUser().getName(),
              p.getTitle(),
              new AddressDTO(c.getUser().getAddress()).getAddressString(),
              p.getPrice(),
              c.getEmail(),
              c.getPhoneNumber());
      rabbitMQSender.send(mess);
    }

    return newProduct;
  }

  public Product update(Product p){
    if(productRepository.existsById(p.getId())){
      return productRepository.save(p);
    }
    return new Product();
  }

  public String delete(Long id){
    productRepository.deleteById(id);
    return "Deleted!";
  }

  public static List<Product> filterByPriceRange(List<Product> products, Integer priceRange){
    List<Product> productsAfterFiltered = new ArrayList<Product>();
    switch (priceRange){
      case 0:
        return  products
                .stream()
                .filter(item -> item.getPrice() <= 500000000)
                .collect(Collectors.toList());
      case 1:
        return  products
                .stream()
                .filter(item -> (item.getPrice() > 500000000)&&(item.getPrice() <= 1000000000))
                .collect(Collectors.toList());
      case 2:
        return   products
                .stream()
                .filter(item -> (item.getPrice() > 1000000000)&&(item.getPrice() <= 1500000000))
                .collect(Collectors.toList());
      case 3:
        return   products
                .stream()
                .filter(item -> (item.getPrice() > 1500000000)&&(item.getPrice() <=2000000000))
                .collect(Collectors.toList());
      default:
        return  products
                .stream()
                .filter(item -> item.getPrice() > 2000000000)
                .collect(Collectors.toList());
    }
  }
  public static List<Product> filterBySearchSTring(List<Product> products, String searchString){

    List<Product> productsAfterFiltered = new ArrayList<Product>();
    for(Product p : products)
      if(p.getTitle().toLowerCase(Locale.ROOT).contains(searchString.toLowerCase(Locale.ROOT)))
        productsAfterFiltered.add(p);

     return productsAfterFiltered;
  }
  public List<Product> search( Integer provinceCityId,
                               Long districtId,
                               Long wardId,
                               Long streetId,
                               Integer priceRange,
                               String searchString){

    List<Product> products = null;

    if(streetId != null)
      products = productRepository.searchByStreet(streetId);
    if(wardId != null && products == null)
      products = productRepository.searchByWard(wardId);
    if(districtId != null && products == null)
      products = productRepository.searchByDistrict(districtId);
    if(provinceCityId != null && products == null)
      products = productRepository.searchByCity(provinceCityId);

    if(priceRange != null){
      if(products == null){
        switch (priceRange){
          case 0: products = productRepository.searchByPrice500(); break;
          case 1: products = productRepository.searchByPrice500_1000(); break;
          case 2: products = productRepository.searchByPrice1000_1500(); break;
          case 3: products = productRepository.searchByPrice1500_2000(); break;
          default: products =  productRepository.searchByPrice2000();
        }
      }else{
        products = filterByPriceRange(products, priceRange);
      }
    }

    if(searchString != null){
      if(products == null)
        products = productRepository.searchByString(searchString);
      else{
        products = filterBySearchSTring(products, searchString);
      }
    }

    return products;
  }

}
