package com.example.sportproapp.presentation.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sportproapp.data.model.MiembroComunidad
import com.example.sportproapp.data.model.PublicacionConAutor
import com.example.sportproapp.presentation.auth.CardBorderColor
import com.example.sportproapp.presentation.auth.DarkBackground
import com.example.sportproapp.presentation.auth.DarkCardBackground
import com.example.sportproapp.presentation.auth.InputBackgroundColor
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuroComunidadScreen(
    navController: NavController,
    viewModel: MuroComunidadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarCrearDialog by remember { mutableStateOf(false) }
    var publicacionAEditar by remember { mutableStateOf<PublicacionConAutor?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarFeed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val stateSuccess = uiState as? MuroUiState.Success
            val academiaSubtitulo = if (stateSuccess != null && stateSuccess.academiaNombre.isNotBlank()) "Comunidad ${stateSuccess.academiaNombre}" else "Comunidad del Club"

            // --- CABECERA SUPERIOR ---
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Muro del Club",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = academiaSubtitulo,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
/*                navigationIcon = {
                    IconButton(onClick = { /* Menú */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menú",
                            tint = Color.White
                        )
                    }
                }, */
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )

            when (val state = uiState) {
                is MuroUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }

                is MuroUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = Color.Red, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.cargarDatos() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime)
                            ) {
                                Text(text = "Reintentar", color = DarkBackground)
                            }
                        }
                    }
                }

                is MuroUiState.Success -> {
                    // Pestaña de Pendientes visible EXCLUSIVAMENTE para el Administrador (ADM)
                    val tabs = remember(state.esAdmin, state.pendientesCount) {
                        val list = mutableListOf("Todos", "Mi equipo", "Anuncios")
                        if (state.esAdmin && state.pendientesCount > 0) {
                            list.add("Pendientes (${state.pendientesCount})")
                        }
                        list
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- 1. FILTROS (Pills) ---
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                items(tabs) { tab ->
                                    val isSelected = state.filtroSeleccionado == tab
                                    FilterChipPill(
                                        text = tab,
                                        isSelected = isSelected,
                                        onClick = { viewModel.seleccionarFiltro(tab) }
                                    )
                                }
                            }
                        }

                        // --- 2. HISTORIAS / AVATARES DE MIEMBROS ---
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                items(state.miembros) { miembro ->
                                    MiembroAvatarItem(miembro = miembro)
                                }
                            }
                        }

                        // --- 3A. BANNER DE REVISIÓN PARA ADMINISTRADOR (EXCLUSIVO ROLE ADM) ---
                        if (state.esAdmin && state.pendientesCount > 0) {
                            item {
                                AdminPendingBanner(
                                    count = state.pendientesCount,
                                    onClickReview = {
                                        val firstPending = state.publicacionesPendientes.firstOrNull()
                                        if (firstPending?.publicacion?.id != null) {
                                            navController.navigate("aprobacion-dt/${firstPending.publicacion.id}")
                                        } else {
                                            viewModel.seleccionarFiltro("Pendientes (${state.pendientesCount})")
                                        }
                                    }
                                )
                            }
                        }

                        // --- 3B. BANNER DE CAMBIOS SOLICITADOS (PARA EL AUTOR DE LA PUBLICACIÓN) ---
                        if (state.misCambiosSolicitados.isNotEmpty()) {
                            item {
                                UserChangesRequestedBanner(
                                    count = state.misCambiosSolicitados.size,
                                    onClickEdit = {
                                        publicacionAEditar = state.misCambiosSolicitados.firstOrNull()
                                    }
                                )
                            }
                        }

                        // --- 4. LISTA DE PUBLICACIONES EN EL FEED ---
                        if (state.publicaciones.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay publicaciones aprobadas en esta sección.",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(state.publicaciones) { pubConAutor ->
                                PublicacionCardItem(
                                    pubConAutor = pubConAutor,
                                    esAdmin = state.esAdmin,
                                    onLikeClick = { viewModel.toggleLike(pubConAutor.publicacion.id ?: "") },
                                    onCommentClick = {
                                        navController.navigate("detalle-post/${pubConAutor.publicacion.id}")
                                    },
                                    onRevisarClick = {
                                        navController.navigate("aprobacion-dt/${pubConAutor.publicacion.id}")
                                    },
                                    onEditarClick = {
                                        publicacionAEditar = pubConAutor
                                    }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // --- BOTÓN FLOTANTE (FAB) PARA NUEVA PUBLICACIÓN ---
        FloatingActionButton(
            onClick = { mostrarCrearDialog = true },
            containerColor = PrimaryLime,
            contentColor = DarkBackground,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nueva Publicación",
                modifier = Modifier.size(32.dp)
            )
        }

        // --- MODAL PARA CREAR PUBLICACIÓN ---
        if (mostrarCrearDialog) {
            CrearPublicacionDialog(
                onDismiss = { mostrarCrearDialog = false },
                onPublicar = { contenido, tipo ->
                    viewModel.crearPublicacion(contenido, tipo)
                    mostrarCrearDialog = false
                }
            )
        }

        // --- MODAL PARA CORREGIR Y REENVIAR PUBLICACIÓN ---
        publicacionAEditar?.let { pubItem ->
            EditarPublicacionDialog(
                pubConAutor = pubItem,
                onDismiss = { publicacionAEditar = null },
                onGuardar = { nuevoTexto ->
                    viewModel.editarYReenviarPublicacion(
                        publicacionId = pubItem.publicacion.id ?: "",
                        nuevoContenido = nuevoTexto
                    )
                    publicacionAEditar = null
                }
            )
        }
    }
}

// --- COMPONENTE: PILL DE FILTRO ---
@Composable
fun FilterChipPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryLime else DarkCardBackground)
            .border(
                width = 1.dp,
                color = if (isSelected) PrimaryLime else CardBorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) DarkBackground else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// --- COMPONENTE: ITEM AVATAR MIEMBRO ---
@Composable
fun MiembroAvatarItem(miembro: MiembroComunidad) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(DarkCardBackground)
                .border(2.dp, if (miembro.esUsuarioActual) PrimaryLime else CardBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!miembro.avatarUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(miembro.avatarUrl),
                    contentDescription = miembro.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = miembro.nombre,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = miembro.nombre,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

// --- COMPONENTE: BANNER DE PUBLICACIONES PENDIENTES (EXCLUSIVO ADMINISTRADOR) ---
@Composable
fun AdminPendingBanner(count: Int, onClickReview: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFC107), RoundedCornerShape(12.dp))
            .clickable { onClickReview() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF26200A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$count publicación(es) pendiente(s) de aprobación",
                    color = Color(0xFFFFC107),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Revisar ›",
                color = PrimaryLime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- COMPONENTE: BANNER DE CAMBIOS SOLICITADOS (PARA EL AUTOR DE LA PUBLICACIÓN) ---
@Composable
fun UserChangesRequestedBanner(count: Int, onClickEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(12.dp))
            .clickable { onClickEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF261D0A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tienes $count publicación con cambios solicitados por el Administrador",
                    color = Color(0xFFFF9800),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Corregir ›",
                color = PrimaryLime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- COMPONENTE: TARJETA DE PUBLICACIÓN EN EL MURO ---
@Composable
fun PublicacionCardItem(
    pubConAutor: PublicacionConAutor,
    esAdmin: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRevisarClick: (() -> Unit)? = null,
    onEditarClick: (() -> Unit)? = null
) {
    val estado = pubConAutor.publicacion.estado
    val esPendiente = estado == "PENDIENTE"
    val esCambiosSolicitados = estado == "CAMBIOS_SOLICITADOS"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                when {
                    esPendiente && esAdmin -> Color(0xFFFFC107)
                    esCambiosSolicitados -> Color(0xFFFF9800)
                    else -> CardBorderColor
                },
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                esPendiente && esAdmin -> Color(0xFF1E1906)
                esCambiosSolicitados -> Color(0xFF261D0A)
                else -> DarkCardBackground
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // AVISO 1: PENDIENTE DE REVISIÓN (Solo visible para el Administrador ADM)
            if (esPendiente && esAdmin) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏳ PENDIENTE DE REVISIÓN",
                        color = Color(0xFFFFC107),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (onRevisarClick != null) {
                        Button(
                            onClick = onRevisarClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "Revisar y Aprobar",
                                color = DarkBackground,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFFFC107).copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // AVISO 2: CAMBIOS SOLICITADOS (Visible para el autor)
            if (esCambiosSolicitados) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ EL ADMINISTRADOR SOLICITÓ CAMBIOS EN ESTA PUBLICACIÓN",
                            color = Color(0xFFFF9800),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (onEditarClick != null) {
                            Button(
                                onClick = onEditarClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "Corregir y Reenviar",
                                    color = DarkBackground,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (!pubConAutor.publicacion.notasInternas.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notas: \"${pubConAutor.publicacion.notasInternas}\"",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFFF9800).copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Cabecera del Autor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(InputBackgroundColor)
                            .border(1.dp, CardBorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pubConAutor.autorNombre,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RolBadge(rol = pubConAutor.autorRol)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pubConAutor.tiempoRelativo,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                /*
                IconButton(onClick = { /* Opciones */ }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "Opciones",
                        tint = TextMuted
                    )
                } */
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contenido Texto
            Text(
                text = pubConAutor.publicacion.contenido,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Imagen opcional si viene en la publicación
            if (!pubConAutor.publicacion.imagenUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(pubConAutor.publicacion.imagenUrl),
                    contentDescription = "Imagen publicación",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // FILA DE CONTADORES: LIKES Y COMENTARIOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${pubConAutor.publicacion.likesCount} me gusta",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${pubConAutor.publicacion.commentsCount} comentarios",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onCommentClick() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Barra de Botones de Acción: Me Gusta, Comentar, Compartir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLikeClick() }
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (pubConAutor.isLikedByCurrentUser) Icons.Outlined.ThumbUp else Icons.Outlined.ThumbUpOffAlt,
                        contentDescription = "Me gusta",
                        tint = if (pubConAutor.isLikedByCurrentUser) PrimaryLime else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Me gusta",
                        color = if (pubConAutor.isLikedByCurrentUser) PrimaryLime else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comentar",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Comentar",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { /* Compartir */ }
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Compartir",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Compartir",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// --- COMPONENTE: BADGE DE ROL ---
@Composable
fun RolBadge(rol: String) {
    val (badgeBg, badgeFg) = when (rol.lowercase()) {
        "entrenador" -> Color(0xFF1E3A1A) to PrimaryLime
        "padre" -> Color(0xFF3D3212) to Color(0xFFFFC107)
        "admin" -> Color(0xFF2E1A3D) to Color(0xFFD080FF)
        "jugador" -> Color(0xFF143026) to Color(0xFF00E676)
        else -> DarkCardBackground to TextMuted
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(badgeBg)
            .border(1.dp, badgeFg.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = rol,
            color = badgeFg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- DIÁLOGO / MODAL PARA CREAR PUBLICACIÓN ---
@Composable
fun CrearPublicacionDialog(
    onDismiss: () -> Unit,
    onPublicar: (contenido: String, tipo: String) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf("GENERAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBackground,
        title = {
            Text(
                text = "Nueva Publicación",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tu publicación será enviada a revisión del Administrador antes de ser visible en el muro.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text("Escribe el mensaje o crónica...", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBackgroundColor,
                        unfocusedContainerColor = InputBackgroundColor,
                        focusedBorderColor = PrimaryLime,
                        unfocusedBorderColor = CardBorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("GENERAL" to "General", "MI_EQUIPO" to "Mi equipo", "ANUNCIO" to "Anuncio").forEach { (key, label) ->
                        val isSelected = tipoSeleccionado == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryLime else InputBackgroundColor)
                                .clickable { tipoSeleccionado = key }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) DarkBackground else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPublicar(texto, tipoSeleccionado) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime)
            ) {
                Text("Enviar para revisión", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}

// --- DIÁLOGO / MODAL PARA CORREGIR Y REENVIAR PUBLICACIÓN ---
@Composable
fun EditarPublicacionDialog(
    pubConAutor: PublicacionConAutor,
    onDismiss: () -> Unit,
    onGuardar: (nuevoTexto: String) -> Unit
) {
    var texto by remember { mutableStateOf(pubConAutor.publicacion.contenido) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBackground,
        title = {
            Text(
                text = "Corregir Publicación",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!pubConAutor.publicacion.notasInternas.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261D0A))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Notas del Administrador:",
                                color = Color(0xFFFF9800),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"${pubConAutor.publicacion.notasInternas}\"",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text("Escribe el mensaje o crónica corregida...", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBackgroundColor,
                        unfocusedContainerColor = InputBackgroundColor,
                        focusedBorderColor = PrimaryLime,
                        unfocusedBorderColor = CardBorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(texto) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime)
            ) {
                Text("Guardar y Reenviar", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}
