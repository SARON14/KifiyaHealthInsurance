package et.com.kifiya.health_insurance.service;

import et.com.kifiya.health_insurance.dto.HealthServiceProviderRequestDto;
import et.com.kifiya.health_insurance.dto.ResponseDto;
import et.com.kifiya.health_insurance.model.HealthServiceProvider;
import et.com.kifiya.health_insurance.model.InsuredEmployee;
import et.com.kifiya.health_insurance.repository.HealthServiceProviderRepository;
import et.com.kifiya.health_insurance.repository.InsuredEmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class HealthServiceProviderService {

    private final HealthServiceProviderRepository healthServiceProviderRepository;
    private final InsuredEmployeeRepository insuredEmployeeRepository;
    private final UtilityService utilityService;

    /**
     * this method registers health centers like clinic, hospitals
     * @param healthServiceProviderRequestDto
     * @return
     */

    public ResponseEntity<ResponseDto> create(HealthServiceProviderRequestDto healthServiceProviderRequestDto) {
        Optional<HealthServiceProvider> healthServiceProviderOptional = healthServiceProviderRepository.findByName(healthServiceProviderRequestDto.getName());
        if (healthServiceProviderOptional.isPresent()) {
            return ResponseEntity.ok(new ResponseDto<HealthServiceProvider>(HttpStatus.OK, "provider name already registered", null));
        }
        HealthServiceProvider healthServiceProvider = new HealthServiceProvider();
        healthServiceProvider.setName(healthServiceProviderRequestDto.getName());
        healthServiceProvider.setAdditionalInformation(healthServiceProviderRequestDto.getAdditionalInformation());
        healthServiceProvider.setFullAddress(healthServiceProviderRequestDto.getAddress());
        healthServiceProviderRepository.save(healthServiceProvider);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "registered successfully", healthServiceProvider));
    }

    /**
     * this methode is used to update the name, address or additional information of health centers
     * @param providerId
     * @param healthServiceProviderRequestDto
     * @return
     */

    public ResponseEntity<ResponseDto> update(Long providerId, HealthServiceProviderRequestDto healthServiceProviderRequestDto) {
        var ref = new Object() {
            ResponseEntity<ResponseDto> responseEntity;
        };
        Optional<HealthServiceProvider> healthServiceProviderOptional = healthServiceProviderRepository.findById(providerId);
        healthServiceProviderOptional.ifPresentOrElse(
                (value) -> {
                    if (healthServiceProviderRequestDto.getName() != null)
                        value.setName(healthServiceProviderRequestDto.getName());
                    if (healthServiceProviderRequestDto.getAddress() != null)
                        value.setFullAddress(healthServiceProviderRequestDto.getAddress());
                    if (healthServiceProviderRequestDto.getAdditionalInformation() != null)
                        value.setAdditionalInformation(healthServiceProviderRequestDto.getAdditionalInformation());
                    healthServiceProviderRepository.save(value);
                    ref.responseEntity = ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "updated successfully", value));
                }, () -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "provider id -> " + providerId + " not found");
                });

        return ref.responseEntity;
    }

    /**
     * this method delete health centers if there is no insured employee under the health centers
     * @param providerId
     * @return
     */

    public ResponseEntity<ResponseDto> delete(Long providerId) {
        var ref = new Object() {
            ResponseEntity<ResponseDto> responseEntity;
        };
        Optional<HealthServiceProvider> optionalHealthServiceProvider = healthServiceProviderRepository.findById(providerId);
        optionalHealthServiceProvider.ifPresentOrElse(
                (value) -> {
                    List<InsuredEmployee> insuredEmployees = insuredEmployeeRepository.findByHealthServiceProvider(value);
                    if (insuredEmployees == null || insuredEmployees.size() == 0) {
                        healthServiceProviderRepository.delete(value);
                        ref.responseEntity = ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "deleted successfully", null));
                    } else
                        ref.responseEntity = ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "delete the insured employees registered under this provider first", null));
                }, () -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "provider id -> " + providerId + " not found");
                }
        );
        return ref.responseEntity;
    }

    /**
     * this method list all the health centers, it can list by order either asc or desc by the parameter given.
     * the parameter could be id, name or address
     * @param page
     * @param size
     * @param param
     * @param order
     * @return
     */

    public ResponseEntity<Page<HealthServiceProvider>> getAll(int page, int size, String param, String order) {
        return ResponseEntity.ok(healthServiceProviderRepository.findAll(utilityService.pageable(page, size, order, param)));
    }

}
