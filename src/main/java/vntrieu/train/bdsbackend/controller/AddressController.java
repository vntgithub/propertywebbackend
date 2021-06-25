package vntrieu.train.bdsbackend.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vntrieu.train.bdsbackend.dto.AddressDTO;
import vntrieu.train.bdsbackend.dto.ProductDTO;
import vntrieu.train.bdsbackend.model.Address;
import vntrieu.train.bdsbackend.service.AddressService;

@AllArgsConstructor
@RestController
@RequestMapping("/address")
public class AddressController {

  private final AddressService addressService;

  @GetMapping("/{user_id}")
  AddressDTO getByUserId(@PathVariable Long user_id) {
    return new AddressDTO(addressService.getByUserId(user_id));
  }

  @PostMapping
  AddressDTO add(@RequestBody Address a) {
    return new AddressDTO(addressService.add(a));
  }

  @PutMapping
  AddressDTO update(@RequestBody Address a) {
    return new AddressDTO(addressService.update(a));
  }

  @DeleteMapping("/{id}")
  String delete(@PathVariable Long id){return addressService.delete(id);}


}

