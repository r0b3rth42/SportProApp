package com.example.sportproapp.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.ComentarioConAutor
import com.example.sportproapp.presentation.auth.CardBorderColor
import com.example.sportproapp.presentation.auth.DarkBackground
import com.example.sportproapp.presentation.auth.DarkCardBackground
import com.example.sportproapp.presentation.auth.InputBackgroundColor
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePostScreen(
    navController: NavController,
    publicacionId: String,
    viewModel: DetallePostViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(publicacionId) {
        viewModel.cargarDetalle(publicacionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Publicación",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Comentarios y respuestas",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            val state = uiState as? DetallePostUiState.Success
            if (state != null) {
                BottomBarComentario(
                    texto = state.textoComentario,
                    responderANombre = state.responderANombre,
                    errorOfensivo = state.errorOfensivo,
                    onValueChange = { viewModel.updateTextoComentario(it) },
                    onCancelarRespuesta = { viewModel.cancelarRespuesta() },
                    onEnviar = { viewModel.enviarComentario() }
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        when (val state = uiState) {
            is DetallePostUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryLime)
                }
            }

            is DetallePostUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = Color.Red, fontSize = 14.sp)
                }
            }

            is DetallePostUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- 1. TARJETA DE LA PUBLICACIÓN ORIGINAL ---
                    item {
                        PublicacionCardItem(
                            pubConAutor = state.publicacionConAutor,
                            esAdmin = false,
                            onLikeClick = { viewModel.toggleLikePost() },
                            onCommentClick = { /* Scroll down o enfocar input */ }
                        )
                    }

                    // --- 2. TÍTULO SECCIÓN COMENTARIOS ---
                    item {
                        Text(
                            text = "COMENTARIOS",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    // --- 3. LISTA DE COMENTARIOS Y RESPUESTAS ---
                    if (state.comentarios.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sé el primero en comentar esta publicación.",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(state.comentarios) { comentarioPadre ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Comentario Principal
                                ComentarioItem(
                                    comentarioConAutor = comentarioPadre,
                                    esRespuesta = false,
                                    onLikeClick = { viewModel.toggleLikeComentario(comentarioPadre.comentario.id ?: "") },
                                    onResponderClick = {
                                        viewModel.seleccionarRespuesta(
                                            comentarioPadre.comentario.id ?: "",
                                            comentarioPadre.autorNombre
                                        )
                                    }
                                )

                                // Respuestas Anidadas (Indentadas)
                                comentarioPadre.respuestas.forEach { respuesta ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Flecha de indentación ↳
                                        Text(
                                            text = "↳",
                                            color = TextMuted,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 10.dp)
                                        )

                                        Box(modifier = Modifier.weight(1f)) {
                                            ComentarioItem(
                                                comentarioConAutor = respuesta,
                                                esRespuesta = true,
                                                onLikeClick = { viewModel.toggleLikeComentario(respuesta.comentario.id ?: "") },
                                                onResponderClick = {
                                                    viewModel.seleccionarRespuesta(
                                                        comentarioPadre.comentario.id ?: "",
                                                        respuesta.autorNombre
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

// --- COMPONENTE: ITEM DE COMENTARIO ---
@Composable
fun ComentarioItem(
    comentarioConAutor: ComentarioConAutor,
    esRespuesta: Boolean,
    onLikeClick: () -> Unit,
    onResponderClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabecera: Avatar, Nombre, Rol, Tiempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(InputBackgroundColor)
                            .border(1.dp, CardBorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comentarioConAutor.autorNombre,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RolBadge(rol = comentarioConAutor.autorRol)
                        }
                    }
                }

                Text(
                    text = comentarioConAutor.tiempoRelativo,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Texto del comentario
            Text(
                text = comentarioConAutor.comentario.contenido,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción: Me gusta & Responder
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ThumbUp,
                        contentDescription = "Me gusta",
                        tint = if (comentarioConAutor.isLikedByCurrentUser) PrimaryLime else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Me gusta",
                        color = if (comentarioConAutor.isLikedByCurrentUser) PrimaryLime else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!esRespuesta) {
                    Text(
                        text = "Responder",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onResponderClick() }
                    )
                }
            }
        }
    }
}

// --- BARRA INFERIOR PARA ESCRIBIR COMENTARIOS CON MODERACIÓN POR IA EN TIEMPO REAL ---
@Composable
fun BottomBarComentario(
    texto: String,
    responderANombre: String?,
    errorOfensivo: String?,
    onValueChange: (String) -> Unit,
    onCancelarRespuesta: () -> Unit,
    onEnviar: () -> Unit
) {
    Surface(
        color = DarkCardBackground,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // 🚨 ADVERTENCIA EN ROJO SI EL AGENTE DE IA DETECTA LENGUAJE OFENSIVO O INAPROPIADO
            if (!errorOfensivo.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, Color(0xFFFF3333), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E0C0C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFFF3333),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorOfensivo,
                            color = Color(0xFFFF3333),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (!responderANombre.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Respondiendo a $responderANombre",
                        color = PrimaryLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onCancelarRespuesta,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cancelar",
                            tint = TextMuted
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(InputBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = texto,
                    onValueChange = onValueChange,
                    placeholder = { Text("Escribe un comentario...", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBackgroundColor,
                        unfocusedContainerColor = InputBackgroundColor,
                        focusedBorderColor = PrimaryLime,
                        unfocusedBorderColor = CardBorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onEnviar,
                    modifier = Modifier
                        .size(44.dp)
                        .background(PrimaryLime, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = DarkBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
