package com.amsal.fidmap.endUser;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/end-user")
public class EndUserController {

    private final EndUserRepository endUserRepository;

    // Lets the widget check "do we already know this person" before showing the
    // name/email capture form, so returning visitors aren't asked again.
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<EndUserDto>> lookup(@RequestParam("email") String email) {

        EndUser endUser = endUserRepository.findEndUserByEmail(email);
        if (endUser == null) {
            return ResponseEntity.status(NOT_FOUND).body(ApiResponse.success("not found", null));
        }

        EndUserDto dto = new EndUserDto();
        dto.setId(null); // EndUserDto.id is a Long but EndUser.id is a UUID — see note below
        dto.setName(endUser.getName());
        dto.setEmail(endUser.getEmail());

        return ResponseEntity.status(OK).body(ApiResponse.success("found", dto));
    }
}

// NOTE: EndUserDto.id is declared as `Long`, but EndUser's actual primary key is a
// `UUID` (GenerationType.UUID). That mismatch means EndUserDto can never actually carry
// an end user's real id. Recommend changing EndUserDto.id to `UUID` and setting it above —
// the frontend needs the real UUID to call DELETE /comment/end-user/{end-user-id}/comment/{id}.