package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.PersonAddAlt1
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CinemaTheme

private enum class AuthFormMode(val label: String) {
    SIGN_UP("Create Account"),
    SIGN_IN("Sign In")
}

@Composable
fun OnboardingAuthScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onEmailSignUpClick: (fullName: String, email: String, password: String, confirmPassword: String) -> Unit,
    onEmailSignInClick: (email: String, password: String) -> Unit,
    onGoogleSignInClick: (isSignUp: Boolean) -> Unit,
    onContinueGuestClick: () -> Unit,
    onDismissError: () -> Unit,
    onCloseClick: (() -> Unit)? = null,
    initialSignUpMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val focusManager = LocalFocusManager.current

    if (onCloseClick != null) {
        BackHandler(onBack = onCloseClick)
    }

    var authMode by rememberSaveable {
        mutableStateOf(if (initialSignUpMode) AuthFormMode.SIGN_UP else AuthFormMode.SIGN_IN)
    }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.gold,
        unfocusedBorderColor = colors.borderSubtle,
        focusedLabelColor = colors.gold,
        unfocusedLabelColor = colors.silver,
        focusedTextColor = colors.alabaster,
        unfocusedTextColor = colors.alabaster,
        cursorColor = colors.gold,
        focusedLeadingIconColor = colors.gold,
        unfocusedLeadingIconColor = colors.silver,
        focusedContainerColor = colors.obsidian.copy(alpha = 0.55f),
        unfocusedContainerColor = colors.obsidian.copy(alpha = 0.4f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.obsidian)
    ) {
        // Atmospheric Cinema Backdrop Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_cinema),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to colors.scrimDark.copy(alpha = 0.28f),
                            0.5f to colors.obsidian.copy(alpha = 0.78f),
                            1.0f to colors.obsidian
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (onCloseClick != null) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 540.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.testTag("auth_modal_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close sign in",
                            tint = colors.alabaster
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.gold.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.45f)),
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MovieFilter,
                            contentDescription = stringResource(id = R.string.app_name),
                            tint = colors.gold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.alabaster,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.silver,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Main Auth Card (Normal Login / Create Account + Google Sign-In)
                Surface(
                    color = colors.slate.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mode Segmented Switch: Create Account vs Sign In
                        Surface(
                            color = colors.obsidian.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AuthFormMode.entries.forEach { mode ->
                                    val selected = authMode == mode
                                    Surface(
                                        color = if (selected) colors.gold else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(enabled = !isBusy) {
                                                authMode = mode
                                                onDismissError()
                                            }
                                            .testTag(
                                                if (mode == AuthFormMode.SIGN_UP) "auth_mode_signup"
                                                else "auth_mode_signin"
                                            )
                                    ) {
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) colors.onGold else colors.silver,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Surface(
                                color = colors.crimsonContainer,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.crimson.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_error_banner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ErrorOutline,
                                        contentDescription = "Authentication error",
                                        tint = colors.crimson,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = errorMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.alabaster,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = onDismissError) {
                                        Text("Dismiss", color = colors.gold)
                                    }
                                }
                            }
                        }

                        if (authMode == AuthFormMode.SIGN_UP) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("Alex Morgan") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Rounded.Badge, contentDescription = null)
                                },
                                singleLine = true,
                                enabled = !isBusy,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("you@example.com") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.Email, contentDescription = null)
                            },
                            singleLine = true,
                            enabled = !isBusy,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            placeholder = { Text(if (authMode == AuthFormMode.SIGN_UP) "Min. 6 characters" else "Enter your password") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (showPassword) "Hide password" else "Show password",
                                        tint = colors.silver
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isBusy,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (authMode == AuthFormMode.SIGN_UP) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = {
                                    focusManager.clearFocus()
                                    if (authMode == AuthFormMode.SIGN_IN) {
                                        onEmailSignInClick(email, password)
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (authMode == AuthFormMode.SIGN_UP) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                placeholder = { Text("Re-enter password") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                enabled = !isBusy,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onEmailSignUpClick(fullName, email, password, confirmPassword)
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input")
                            )
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (authMode == AuthFormMode.SIGN_UP) {
                                    onEmailSignUpClick(fullName, email, password, confirmPassword)
                                } else {
                                    onEmailSignInClick(email, password)
                                }
                            },
                            enabled = !isBusy,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.gold,
                                contentColor = colors.onGold,
                                disabledContainerColor = colors.gold.copy(alpha = 0.55f),
                                disabledContentColor = colors.onGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag(
                                    if (authMode == AuthFormMode.SIGN_UP) "onboarding_normal_signup_button"
                                    else "onboarding_normal_signin_button"
                                )
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.onGold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Please wait…",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = if (authMode == AuthFormMode.SIGN_UP) {
                                        Icons.Rounded.PersonAddAlt1
                                    } else {
                                        Icons.AutoMirrored.Rounded.Login
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (authMode == AuthFormMode.SIGN_UP) "Create Account" else "Sign In",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderSubtle)
                            Text(
                                text = "OR",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.ash,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderSubtle)
                        }

                        OutlinedButton(
                            onClick = { onGoogleSignInClick(authMode == AuthFormMode.SIGN_UP) },
                            enabled = !isBusy,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.72f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.alabaster,
                                containerColor = colors.obsidian.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_google_signin_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.gold.copy(alpha = 0.18f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.gold,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google (Cloud Sync)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (onCloseClick == null) {
                    TextButton(
                        onClick = onContinueGuestClick,
                        enabled = !isBusy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboarding_guest_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonOutline,
                            contentDescription = null,
                            tint = colors.silver,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue as Guest",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.silver,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Compact Feature Highlights Card
                Surface(
                    color = colors.slate.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OnboardingHighlightRow(
                            icon = Icons.Rounded.AutoAwesome,
                            tint = colors.gold,
                            title = "Dual-Source Cinema Intelligence",
                            subtitle = "TMDB discovery with live IMDb, Rotten Tomatoes & Metacritic ratings."
                        )
                        OnboardingHighlightRow(
                            icon = Icons.Rounded.CloudSync,
                            tint = colors.emerald,
                            title = "Device Profile & Cloud Backup",
                            subtitle = "Create a local email account or connect Google anytime to sync across devices."
                        )
                        OnboardingHighlightRow(
                            icon = Icons.Rounded.Storage,
                            tint = colors.indigo,
                            title = "Portable JSON Vault",
                            subtitle = "Export and import your complete watch history, reviews & analytics."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingHighlightRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    val colors = CinemaTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.alabaster,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.silver
            )
        }
    }
}
