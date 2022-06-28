package it.aman.authenticationservice.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Sets;

import it.aman.authentication_service.client.model.ModelUser;
import it.aman.authenticationservice.dal.entity.AuthAccount;
import it.aman.authenticationservice.dal.entity.AuthRole;
import it.aman.authenticationservice.dal.entity.AuthUser;
import it.aman.authenticationservice.dal.repository.RoleRepository;
import it.aman.authenticationservice.dal.repository.UserRepository;
import it.aman.authenticationservice.util.FileUtils;
import it.aman.authenticationservice.util.mapper.UserAccountMapper;
import it.aman.common.annotation.Loggable;
import it.aman.common.exception.ERPException;
import it.aman.common.exception.ERPExceptionEnums;
import it.aman.common.util.ERPConstants;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl extends AbstractService {

    private final UserRepository userRepo;

    private final PasswordEncoder passwordEncoder;

    private final UserAccountMapper userMapper;

    private final RoleRepository roleRepository;

    @Value("${app.profile-image.upload-dir:../document}")
    private String imageUploadDir;

    @Value("${app.profile-image.file-types:jpg}")
    private String[] fileTypes;

    @Value("${app.profile-image.width-height:300}")
    private int[] widthHeight;

    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserDetails(ModelUser userInfo) throws ERPException {
        boolean success = false;

        try {
            AuthUser newUser = userMapper.map(userInfo);
            if (Objects.isNull(newUser) || Objects.isNull(newUser.getAccount())) {
                throw ERPExceptionEnums.VALIDATION_EXCEPTION.get();
            }

            // validate if username already used, empty or anonymous
            String username = Optional.ofNullable(newUser.getAccount()).map(AuthAccount::getEmail).map(String::trim)
                    .orElseGet(() -> StringUtils.EMPTY);
            if (StringUtils.EMPTY.equals(username) 
                    || ERPConstants.ANONYMOUS_USER.equals(username)
                    || userRepo.existsByAccountEmail(username)) {
                throw ERPExceptionEnums.USERNAME_TAKEN_EXCEPTION.get();
            }

            OffsetDateTime now = OffsetDateTime.now();
            newUser.setCreatedAt(now);
            newUser.setModifiedAt(now);

            AuthAccount account = newUser.getAccount();
            account.setCreatedAt(now);
            account.setLastAccess(now);
            account.setEnabled(true);
            account.setPassword(passwordEncoder.encode(userInfo.getAccount().getPassword()));

            // NOTE: All registered users are assigned USER role
            AuthRole role = roleRepository.findByName("USER").orElseThrow(ERPExceptionEnums.ROLE_NOT_FOUND);
            account.setEpsRoles(Sets.newHashSet(role));

            newUser.setAccount(account);
            userRepo.save(newUser);
            success = true;
        } catch (DataAccessException e) {
            throw ERPExceptionEnums.VALIDATION_EXCEPTION.get();
        } catch (Exception e) {
            throw e;
        }
        return success;
    }

    // TODO validate ownership before edit
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProfileAvatar(Integer userId, MultipartFile file) throws ERPException, IOException {
        boolean success = false;
        try {
            OffsetDateTime now = OffsetDateTime.now();
            AuthUser user = userRepo.findById(userId).orElseThrow(ERPExceptionEnums.USER_NOT_FOUND);

            // filename [filename + username + date]
            String filename = FilenameUtils.getBaseName(file.getOriginalFilename());
            String validateType = FileUtils.validateFileType(file, fileTypes);
            String trimedFileName = StringUtils.deleteWhitespace(
                    StringUtils.join(filename, user.getFirstName(), LocalDate.now(), ".", validateType));

            byte[] imageByte = FileUtils.resizeImageAsThumbnails(file.getInputStream(), validateType, widthHeight);

            if (imageByte.length > 0) {
                Path fileOut = FileUtils.uploadFileToPath(trimedFileName, imageUploadDir, imageByte);

                String modifiedBy = getCurrentLoggedInUsername();
                user.setModifiedBy(modifiedBy);
                user.setModifiedAt(now);

                user.getAccount().setAvatar(fileOut.toString());
                user.getAccount().setModifiedBy(modifiedBy);
                user.getAccount().setModifiedAt(now);

                userRepo.save(user);
                success = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return success;
    }
}
