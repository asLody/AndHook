/* Cydia Substrate - Powerful Code Insertion Platform
 * Copyright (C) 2008-2011  Jay Freeman (saurik)
 */
/*
 *	@author : rrrfff@foxmail.com
 *  https://github.com/rrrfff/And64InlineHook
 */
/*
 *
 *	@author : rrrfff@foxmail.com
 *  https://github.com/rrrfff/AndHook
 *
 */
#include "../AndHook.h"

#if	!defined(__aarch64__)

// # ifdef __GNUC__
// #  ifdef __clang__
// #   pragma clang diagnostic ignored "-Wabsolute-value"
// #  elif defined(__INTEL_COMPILER)
// // ignored
// #  else
// #   pragma gcc diagnostic ignored "-Wabsolute-value"
// #   define __cdecl //__attribute__((__cdecl__))
// #  endif
// # else
// // ignored
// # endif

# include "Substrate/SubstratePosixMemory.cpp"
# include "Substrate/SubstrateHook.cpp"
# include "Substrate/hde64.c"

#else // __aarch64__

# include "And64InlineHook/And64InlineHook.hpp"
extern "C" JNIEXPORT void MSHookFunction(void *symbol, void *replace, void **result)
{
	return A64HookFunction(symbol, replace, result);
}

#endif // !defined(__aarch64__)