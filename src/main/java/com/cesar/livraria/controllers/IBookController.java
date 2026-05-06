package com.cesar.livraria.controllers;

import com.cesar.livraria.entities.Genre;
import com.cesar.livraria.request.BookRequest;
import com.cesar.livraria.response.BookResponse;
import com.cesar.livraria.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Livros", description = "Operações de gerenciamento do catálogo de livros")
public interface IBookController {

    @PostMapping
    @Operation(
            summary = "Cadastrar livro",
            description = "Cria um novo livro no catálogo. O ISBN deve ser único; caso já exista, a requisição é rejeitada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Livro criado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ISBN já cadastrado ou dados inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            )
    })
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest req);

    @GetMapping
    @Operation(
            summary = "Listar livros (paginado)",
            description = "Retorna uma página de livros, com filtro opcional por gênero. "
                    + "Os parâmetros de paginação seguem o padrão do Spring: page, size e sort (ex.: sort=title,asc)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de livros retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<BookResponse>> listBooks(
            @Parameter(description = "Filtro por gênero (igualdade exata)", example = "FANTASIA")
            @RequestParam(required = false) Genre genre,
            @ParameterObject
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable
    ) ;

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar livro por ID",
            description = "Retorna os dados de um livro a partir do seu identificador único."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            )
    })
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "Identificador único do livro", example = "664f1b2c3e4a5f6b7c8d9e0f", required = true)
            @PathVariable String id
    ) ;

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover livro",
            description = "Remove permanentemente um livro do catálogo pelo seu identificador único."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Livro removido com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            )
    })
    public ResponseEntity<Void> deleteBookById(
            @Parameter(description = "Identificador único do livro", example = "664f1b2c3e4a5f6b7c8d9e0f", required = true)
            @PathVariable String id
    ) ;

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar livro",
            description = "Substitui integralmente os dados de um livro existente. Todos os campos obrigatórios devem ser enviados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos no corpo da requisição",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            )
    })
    public ResponseEntity<BookResponse> updateBookById(
            @Parameter(description = "Identificador único do livro", example = "664f1b2c3e4a5f6b7c8d9e0f", required = true)
            @PathVariable String id,
            @Valid @RequestBody BookRequest req
    ) ;

}
