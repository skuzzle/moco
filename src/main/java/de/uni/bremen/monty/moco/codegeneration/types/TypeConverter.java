/*
 * moco, the Monty Compiler
 * Copyright (c) 2013-2014, Monty's Coconut, All rights reserved.
 *
 * This file is part of moco, the Monty Compiler.
 *
 * moco is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * moco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Linking this program and/or its accompanying libraries statically or
 * dynamically with other modules is making a combined work based on this
 * program. Thus, the terms and conditions of the GNU General Public License
 * cover the whole combination.
 *
 * As a special exception, the copyright holders of moco give
 * you permission to link this programm and/or its accompanying libraries
 * with independent modules to produce an executable, regardless of the
 * license terms of these independent modules, and to copy and distribute the
 * resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module.
 *
 * An independent module is a module which is not
 * derived from or based on this program and/or its accompanying libraries.
 * If you modify this library, you may extend this exception to your version of
 * the program or library, but you are not obliged to do so. If you do not wish
 * to do so, delete this exception statement from your version.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library.
 */

package de.uni.bremen.monty.moco.codegeneration.types;

import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.array;
import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.function;
import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.int64;
import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.int8;
import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.pointer;
import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.struct;
import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.voidType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.codegeneration.context.CodeContext;
import de.uni.bremen.monty.moco.codegeneration.context.CodeContext.Linkage;
import de.uni.bremen.monty.moco.codegeneration.identifier.LLVMIdentifier;
import de.uni.bremen.monty.moco.codegeneration.identifier.LLVMIdentifierFactory;
import de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.LLVMBool;
import de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.LLVMDouble;
import de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.LLVMInt;
import de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.LLVMInt8;

public class TypeConverter {
	private final Map<TypeDeclaration, LLVMType> typeMap = new HashMap<>();
	private final LLVMIdentifierFactory llvmIdentifierFactory;
	private final CodeContext constantContext;

	public TypeConverter(LLVMIdentifierFactory llvmIdentifierFactory, CodeContext constantContext) {
		this.llvmIdentifierFactory = llvmIdentifierFactory;
		this.constantContext = constantContext;
		initPreDefinedTypes();
	}

	private void initPreDefinedTypes() {
		this.typeMap.put(CoreClasses.stringType(), pointer(int8()));
		this.typeMap.put(CoreClasses.voidType(), voidType());
	}

	private LLVMPointer<LLVMFunctionType> convertType(ProcedureDeclaration type) {
		List<LLVMType> parameter = new ArrayList<>();
		final ASTNode grandFatherNode = type.getParentNode().getParentNode();
		if (grandFatherNode instanceof ClassDeclaration) {
			ClassDeclaration typeDeclaration = (ClassDeclaration) grandFatherNode;
			parameter.add(mapToLLVMType(typeDeclaration));
		}
		for (VariableDeclaration varDecl : type.getParameter()) {
			parameter.add(mapToLLVMType(varDecl.getTypeDeclaration()));
		}
		if (type instanceof FunctionDeclaration) {
			FunctionDeclaration func = (FunctionDeclaration) type;
			final TypeDeclaration returnType = func.getReturnTypeIdentifier().getTypeDeclaration();
			return pointer(function(mapToLLVMType(returnType), parameter));
		}
		return pointer(function(voidType(), parameter));
	}

	private LLVMPointer<LLVMStructType> convertType(ClassDeclaration type) {
		return pointer(struct(type.getMangledIdentifier().getSymbol()));
	}

	public TypeDeclaration mapToBoxedType(LLVMType type) {
		if (type instanceof LLVMBool) {
			return CoreClasses.boolType();
		} else if (type instanceof LLVMInt) {
			return CoreClasses.intType();
		} else if (type instanceof LLVMDouble) {
			return CoreClasses.floatType();
		} else if (type instanceof LLVMInt8) {
			return CoreClasses.charType();
		}
		return null;
	}

	private <T extends LLVMType> T convertType(TypeDeclaration type) {
		if (type instanceof ProcedureDeclaration) {
			return (T) convertType((ProcedureDeclaration) type);
		}
		return (T) convertType((ClassDeclaration) type);
	}

	private void addType(TypeDeclaration typeDecl) {
		if (typeDecl == CoreClasses.arrayType()) {
			addArray(typeDecl);
		} else if (typeDecl instanceof ClassDeclaration) {
			addClass((ClassDeclaration) typeDecl);
		}
	}

	private void addClass(ClassDeclaration classDecl) {
		String mangledNodeName = classDecl.getMangledIdentifier().getSymbol();
		LLVMStructType llvmClassType = struct(classDecl.getMangledIdentifier().getSymbol());
		List<LLVMType> llvmClassTypeDeclarations = new ArrayList<>();

		LLVMStructType llvmVMTType = struct(mangledNodeName + "_vmt_type");
		List<LLVMType> llvmVMTTypeDeclarations = new ArrayList<>();
		llvmClassTypeDeclarations.add(pointer(llvmVMTType));

		LLVMIdentifier<LLVMType> llvmVMTDataIdentifier =
		        this.llvmIdentifierFactory.newGlobal(mangledNodeName + "_vmt_data", (LLVMType) llvmVMTType);
		List<LLVMIdentifier<LLVMType>> llvmVMTDataInitializer = new ArrayList<>();

		List<ClassDeclaration> recursiveSuperClassDeclarations = classDecl.getSuperClassDeclarationsRecursive();
		LLVMArrayType llvmCTDataType = array(pointer(int8()), recursiveSuperClassDeclarations.size() + 1);
		LLVMIdentifier<LLVMType> llvmCTDataIdentifier =
		        this.llvmIdentifierFactory.newGlobal(mangledNodeName + "_ct_data", (LLVMType) llvmCTDataType);
		List<LLVMIdentifier<LLVMType>> llvmCTDataInitializer = new ArrayList<>();

		llvmVMTTypeDeclarations.add(pointer(llvmCTDataType));
		llvmVMTDataInitializer.add((LLVMIdentifier<LLVMType>) (LLVMIdentifier<?>) this.llvmIdentifierFactory.pointerTo(llvmCTDataIdentifier));

		for (ClassDeclaration classDeclaration : recursiveSuperClassDeclarations) {
			// Ensure that addType() was called for this classDeclaration so that a VMT/CT was generated.
			mapToLLVMType(classDeclaration);
			LLVMIdentifier<LLVMType> vmtDataIdent =
			        this.llvmIdentifierFactory.newGlobal(
			                classDeclaration.getMangledIdentifier().getSymbol() + "_vmt_data",
			                (LLVMType) pointer(struct(classDeclaration.getMangledIdentifier().getSymbol() + "_vmt_type")));
			llvmCTDataInitializer.add(this.llvmIdentifierFactory.bitcast(vmtDataIdent, pointer(int8())));
		}
		llvmCTDataInitializer.add((LLVMIdentifier<LLVMType>) (LLVMIdentifier<?>) this.llvmIdentifierFactory.constantNull(pointer(int8())));

		if (classDecl == CoreClasses.intType()) {
			llvmClassTypeDeclarations.add(LLVMTypeFactory.int64());
		} else if (classDecl == CoreClasses.boolType()) {
			llvmClassTypeDeclarations.add(LLVMTypeFactory.int1());
		} else if (classDecl == CoreClasses.floatType()) {
			llvmClassTypeDeclarations.add(LLVMTypeFactory.double64());
		} else if (classDecl == CoreClasses.charType()) {
			llvmClassTypeDeclarations.add(LLVMTypeFactory.int8());
		}

		for (ClassDeclaration classDeclaration : recursiveSuperClassDeclarations) {
			for (Declaration decl : classDeclaration.getBlock().getDeclarations()) {
				if (decl instanceof VariableDeclaration) {
					llvmClassTypeDeclarations.add(mapToLLVMType(((VariableDeclaration) decl).getTypeDeclaration()));
				}
			}
		}
		for (ProcedureDeclaration procedure : classDecl.getVirtualMethodTable()) {
			if (!procedure.isInitializer()) {
				LLVMType signature = mapToLLVMType(procedure);
				llvmVMTTypeDeclarations.add(signature);
				llvmVMTDataInitializer.add(this.llvmIdentifierFactory.newGlobal(
				        procedure.getMangledIdentifier().getSymbol(),
				        signature));
			}
		}
		this.constantContext.type(llvmVMTType, llvmVMTTypeDeclarations);
		this.constantContext.type(llvmClassType, llvmClassTypeDeclarations);
		this.constantContext.global(
		        Linkage.priv,
		        llvmCTDataIdentifier,
		        true,
		        this.llvmIdentifierFactory.constant(llvmCTDataType, llvmCTDataInitializer));
		this.constantContext.global(
		        Linkage.priv,
		        llvmVMTDataIdentifier,
		        true,
		        this.llvmIdentifierFactory.constant(llvmVMTType, llvmVMTDataInitializer));
	}

	private void addArray(TypeDeclaration typeDecl) {
		// Temporary until object or generic
		LLVMType llvmType = mapToLLVMType((TypeDeclaration) CoreClasses.intType());
		List<LLVMType> list = Arrays.asList(int64(), array(llvmType, 0));
		LLVMStructType type = struct(typeDecl.getMangledIdentifier().getSymbol());
		this.constantContext.type(type, list);
	}

	public LLVMPointer<LLVMStructType> mapToLLVMType(ClassDeclaration type) {
		return (LLVMPointer<LLVMStructType>) mapToLLVMType((TypeDeclaration) type);
	}

	public LLVMPointer<LLVMFunctionType> mapToLLVMType(ProcedureDeclaration type) {
		return (LLVMPointer<LLVMFunctionType>) mapToLLVMType((TypeDeclaration) type);
	}

	public <T extends LLVMType> T mapToLLVMType(TypeDeclaration type) {
		T llvmType = (T) this.typeMap.get(type);
		if (llvmType == null) {
			llvmType = convertType(type);
			this.typeMap.put(type, llvmType);
			addType(type);
		}
		return llvmType;
	}
}
